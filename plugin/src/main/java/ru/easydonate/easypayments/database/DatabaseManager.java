package ru.easydonate.easypayments.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.config.Configuration;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Logger;

public final class DatabaseManager {

    private final Logger logger;
    private final Configuration config;
    private final Database database;

    private final ConnectionSource connectionSource;
    private final ExecutorService asyncExecutorService;

    @Getter(AccessLevel.PRIVATE) private final Dao<Customer, String> customersDao;
    @Getter(AccessLevel.PRIVATE) private final Dao<Payment, Integer> paymentsDao;
    @Getter(AccessLevel.PRIVATE) private final Dao<Purchase, Integer> purchasesDao;

    public DatabaseManager(@NotNull Plugin plugin, @NotNull Configuration config, @NotNull Database database) throws SQLException {
        this.logger = plugin.getLogger();
        this.config = config;
        this.database = database;

        this.connectionSource = database.establishConnection();
        this.asyncExecutorService = Executors.newCachedThreadPool();

        this.customersDao = DaoManager.createDao(connectionSource, Customer.class);
        this.paymentsDao = DaoManager.createDao(connectionSource, Payment.class);
        this.purchasesDao = DaoManager.createDao(connectionSource, Purchase.class);
    }

    public void shutdown() {
        if(asyncExecutorService != null)
            asyncExecutorService.shutdown();

        if(connectionSource != null)
            connectionSource.closeQuietly();
    }

    public @NotNull DatabaseType getDatabaseType() {
        return database.getDatabaseType();
    }

    public @NotNull CompletableFuture<Integer> transferCustomersDataFrom(@NotNull DatabaseManager sourceStorage) {
        return transferDataFrom(sourceStorage, DatabaseManager::getCustomersDao);
    }

    public @NotNull CompletableFuture<Integer> transferPaymentsDataFrom(@NotNull DatabaseManager sourceStorage) {
        return transferDataFrom(sourceStorage, DatabaseManager::getPaymentsDao);
    }

    public @NotNull CompletableFuture<Integer> transferPurchasesDataFrom(@NotNull DatabaseManager sourceStorage) {
        return transferDataFrom(sourceStorage, DatabaseManager::getPurchasesDao);
    }

    private <T, ID> @NotNull CompletableFuture<Integer> transferDataFrom(
            @NotNull DatabaseManager sourceStorage,
            @NotNull Function<DatabaseManager, Dao<T, ID>> daoExtractor
    ) {
        return supplyAsync(() -> {
            Dao<T, ID> sourceDao = daoExtractor.apply(sourceStorage);
            Dao<T, ID> destinationDao = daoExtractor.apply(this);

            List<T> entries = sourceDao.queryForAll();
            for(T entry : entries) {
                destinationDao.createIfNotExists(entry);
            }

            return entries.size();
        });
    }

    // --- customers
    public @NotNull CompletableFuture<Customer> getCustomerByName(@NotNull String playerName) {
        return supplyAsync(() -> customersDao.queryForId(playerName));
    }

    public @NotNull CompletableFuture<Customer> getCustomerByUUID(@NotNull UUID playerUUID) {
        return supplyAsync(() -> customersDao.queryBuilder()
                .where()
                .eq(Customer.COLUMN_PLAYER_UUID, playerUUID)
                .queryForFirst());
    }

    public @NotNull CompletableFuture<Customer> getCustomer(@NotNull OfflinePlayer bukkitPlayer) {
        if(isUuidIdentificationEnabled())
            return getCustomerByName(bukkitPlayer.getName());
        else
            return getCustomerByUUID(bukkitPlayer.getUniqueId());
    }

    public @NotNull CompletableFuture<Customer> getOrCreateCustomer(@NotNull OfflinePlayer bukkitPlayer) {
        return getOrCreateCustomer(bukkitPlayer, bukkitPlayer.getName());
    }

    public @NotNull CompletableFuture<Customer> getOrCreateCustomer(@NotNull OfflinePlayer bukkitPlayer, @NotNull String playerName) {
        return getCustomerByName(playerName).thenApply(customer -> {
            if(customer == null) {
                customer = new Customer(playerName, bukkitPlayer.getUniqueId());
                saveCustomer(customer).join();
            }

            return customer;
        });
    }

    public @NotNull CompletableFuture<Void> transferCustomerOwnership(@NotNull Customer customer, @NotNull String playerName) {
        return runAsync(() -> customersDao.updateId(customer, playerName));
    }

    public @NotNull CompletableFuture<Void> refreshCustomer(@NotNull Customer customer) {
        return runAsync(() -> customersDao.refresh(customer));
    }

    public @NotNull CompletableFuture<Void> saveCustomer(@NotNull Customer customer) {
        return runAsync(() -> customersDao.createOrUpdate(customer));
    }

    // --- payment
    public @NotNull CompletableFuture<List<Payment>> getAllUnreportedPayments(int serverId) {
        return supplyAsync(() -> paymentsDao.queryBuilder().where()
                .eq(Payment.COLUMN_SERVER_ID, serverId).and()
                .isNull(Payment.COLUMN_REPORTED_AT)
                .query()
        );
    }

    public @NotNull CompletableFuture<Payment> getPayment(int paymentId) {
        return supplyAsync(() -> paymentsDao.queryForId(paymentId));
    }

    public @NotNull CompletableFuture<Void> refreshPayment(@NotNull Payment payment) {
        return runAsync(() -> paymentsDao.refresh(payment));
    }

    public @NotNull CompletableFuture<Void> savePayment(@NotNull Payment payment) {
        return runAsync(() -> paymentsDao.createOrUpdate(payment));
    }

    // --- purchases
    public @NotNull CompletableFuture<Purchase> getPurchase(int purchaseId) {
        return supplyAsync(() -> purchasesDao.queryForId(purchaseId));
    }

    public @NotNull CompletableFuture<Void> savePurchase(@NotNull Purchase purchase) {
        return runAsync(() -> purchasesDao.createOrUpdate(purchase));
    }

    // --- async proxied methods
    private @NotNull CompletableFuture<Void> runAsync(@NotNull ThrowableRunnable task) {
        return CompletableFuture.runAsync(() -> {
            try {
                task.run();
            } catch (SQLException ex) {
                handleThrowable(ex);
            }
        }, asyncExecutorService);
    }

    private <T> @NotNull CompletableFuture<T> supplyAsync(@NotNull ThrowableSupplier<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.supply();
            } catch (SQLException ex) {
                handleThrowable(ex);
                return null;
            }
        }, asyncExecutorService);
    }

    private void handleThrowable(@NotNull Throwable throwable) {
        logger.severe("An error has occurred when this plugin tried to handle an SQL statement!");
        Throwable cause = throwable;
        while(cause != null) {
            logger.severe(cause.toString());
            cause = cause.getCause();
        }
    }

    public boolean isUuidIdentificationEnabled() {
        return config.getBoolean("identify-by-uuid", false);
    }

    @FunctionalInterface
    private interface ThrowableRunnable {
        void run() throws SQLException;
    }

    @FunctionalInterface
    private interface ThrowableSupplier<T> {
        @Nullable T supply() throws SQLException;
    }

}
