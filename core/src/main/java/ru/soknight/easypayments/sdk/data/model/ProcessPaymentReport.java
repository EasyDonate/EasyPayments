package ru.soknight.easypayments.sdk.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
@NoArgsConstructor
public class ProcessPaymentReport implements Comparable<ProcessPaymentReport> {

    @SerializedName("id")
    private int paymentId;
    @SerializedName("payload")
    private String payload;
    @SerializedName("report")
    private List<ExecutedCommand> executedCommands;

    private transient int failedAttempts;

    private ProcessPaymentReport(int paymentId, String payload) {
        this.paymentId = paymentId;
        this.payload = payload;
        this.executedCommands = new CopyOnWriteArrayList<>();
    }

    public static ProcessPaymentReport create(ProcessPayment payment) {
        return new ProcessPaymentReport(payment.getPaymentId(), payment.getPayload());
    }

    @Override
    public int compareTo(ProcessPaymentReport report) {
        return Integer.compare(paymentId, report.getPaymentId());
    }

    public void addExecutedCommand(ExecutedCommand command) {
        if(executedCommands == null)
            executedCommands = new ArrayList<>();

        executedCommands.add(command);
    }

    public void addExecutedCommand(String command, String response) {
        addExecutedCommand(new ExecutedCommand(command, response));
    }

    public void increaseFailedAttemptsCounter() {
        ++failedAttempts;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        ProcessPaymentReport that = (ProcessPaymentReport) o;
        return paymentId == that.paymentId &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(executedCommands, that.executedCommands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, payload, executedCommands);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ExecutedCommand {

        @SerializedName("command")
        private String command;
        @SerializedName("response")
        private String response;

        public ExecutedCommand create(String command, String response) {
            return new ExecutedCommand(command, response);
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            ExecutedCommand that = (ExecutedCommand) o;
            return Objects.equals(command, that.command) &&
                    Objects.equals(response, that.response);
        }

        @Override
        public int hashCode() {
            return Objects.hash(command, response);
        }

    }

}
