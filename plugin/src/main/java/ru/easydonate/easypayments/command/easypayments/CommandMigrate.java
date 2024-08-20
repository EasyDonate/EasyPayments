package ru.easydonate.easypayments.command.easypayments;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.command.CommandExecutor;
import ru.easydonate.easypayments.command.annotation.*;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.core.config.localized.Messages;
import ru.easydonate.easypayments.database.Database;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.DatabaseType;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.database.persister.LocalDateTimePersister;
import ru.easydonate.easypayments.exception.CredentialsParseException;
import ru.easydonate.easypayments.exception.DriverLoadException;
import ru.easydonate.easypayments.exception.DriverNotFoundException;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Command("migrate")
@Arguments("database-type")
@MinimalArgsCount(1)
@PluginEnableRequired
@Permission("easypayments.command.migrate")
public final class CommandMigrate extends CommandExecutor {

    private static final List<String> AVAILABLE_TARGETS = Arrays.asList("sqlite", "h2", "mysql", "postgresql");

    private final EasyPaymentsPlugin plugin;
    private final Configuration config;

    public CommandMigrate(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull Configuration config,
            @NotNull Messages messages
    ) throws InitializationException {
        super(messages);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void executeCommand(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        validateExecution(sender, args);

        if (!EasyPaymentsPlugin.isStorageAvailable())
            throw new ExecutionException(messages.get("migrate.failed.storage-unavailable"));

        String rawType = args.get(0);
        DatabaseType destinationType = DatabaseType.getByKey(rawType);

        if (destinationType.isUnknown())
            throw new ExecutionException(messages.get("migrate.failed.unknown-target"));

        DatabaseType sourceType = plugin.getStorage().getDatabaseType();
        if (destinationType == sourceType)
            throw new ExecutionException(messages.get("migrate.failed.same-target"));

        messages.getAndSend(sender, "migrate.starting",
                "%from%", sourceType.getName(),
                "%to%", destinationType.getName()
        );

        CompletableFuture.runAsync(() -> migrateData(sender, destinationType));
    }

    private synchronized void migrateData(@NotNull CommandSender sender, @NotNull DatabaseType destinationType) {
        try {
            Database database = new Database(plugin, config, destinationType)
                    .registerTable(Customer.class)
                    .registerTable(Payment.class)
                    .registerTable(Purchase.class)
                    .registerPersister(LocalDateTimePersister.getSingleton())
                    .complete();

            DatabaseManager destinationStorage = new DatabaseManager(plugin, config, database);
            DatabaseManager sourceStorage = plugin.getStorage();

            try {
                CompletableFuture<Integer> customers = destinationStorage.transferCustomersDataFrom(sourceStorage);
                CompletableFuture<Integer> payments = destinationStorage.transferPaymentsDataFrom(sourceStorage);
                CompletableFuture<Integer> purchases = destinationStorage.transferPurchasesDataFrom(sourceStorage);

                CompletableFuture.allOf(customers, payments, purchases).join();
                destinationStorage.shutdown();

                int customersAmount = outboxSafety(customers);
                int paymentsAmount = outboxSafety(payments);
                int purchasesAmount = outboxSafety(purchases);

                messages.getAndSend(sender, "migrate.success",
                        "%from%", sourceStorage.getDatabaseType().getName(),
                        "%to%", destinationStorage.getDatabaseType().getName(),
                        "%customers_amount%", customersAmount,
                        "%payments_amount%", paymentsAmount,
                        "%purchases_amount%", purchasesAmount
                );
            } catch (Exception ex) {
                messages.getAndSend(sender, "migrate.failed.unexpected-error", "%message%", ex);
                plugin.getDebugLogger().error("[Migration] Unexpected error");
                plugin.getDebugLogger().error(ex);
            }
        } catch (CredentialsParseException ex) {
            messages.getAndSend(sender, "migrate.failed.invalid-credentials");
            plugin.getDebugLogger().error("[Migration] Credentials error");
        } catch (DriverNotFoundException | DriverLoadException ex) {
            messages.getAndSend(sender, "migrate.failed.driver-load-failed");
            plugin.getDebugLogger().error("[Migration] Driver error");
            plugin.getDebugLogger().error(ex);
        } catch (SQLException ex) {
            Throwable cause = ex;
            while(cause != null) {
                if (cause instanceof ConnectException) {
                    messages.getAndSend(sender, "migrate.failed.invalid-credentials");
                    return;
                }
                cause = cause.getCause();
            }

            messages.getAndSend(sender, "migrate.failed.connection-failed", "%message%", ex);
            plugin.getDebugLogger().error("[Migration] SQL error");
            plugin.getDebugLogger().error(ex);
        }
    }

    @Override
    public @Nullable List<String> provideTabCompletions(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        validateExecution(sender, args);

        if (!EasyPaymentsPlugin.isStorageAvailable() || args.size() != 1)
            return null;

        String arg = args.get(0).toLowerCase();
        String current = plugin.getStorage().getDatabaseType().getKey();

        return AVAILABLE_TARGETS.stream()
                .filter(s -> !s.equals(current))
                .filter(s -> s.startsWith(arg))
                .collect(Collectors.toList());
    }

    private int outboxSafety(@NotNull CompletableFuture<Integer> future) {
        Integer value = future.join();
        return value != null ? value : 0;
    }

}
