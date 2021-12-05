package ru.easydonate.easypayments.sdk.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessPaymentReports {

    @SerializedName("payments")
    private List<ProcessPaymentReport> reports;

    public static ProcessPaymentReports empty() {
        return create(new CopyOnWriteArrayList<>());
    }

    public static ProcessPaymentReports create(List<ProcessPaymentReport> reports) {
        return new ProcessPaymentReports(reports);
    }

    public void addReport(ProcessPaymentReport report) {
        reports.add(report);
    }

    public boolean isEmpty() {
        return reports.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        ProcessPaymentReports report = (ProcessPaymentReports) o;
        return Objects.equals(reports, report.reports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reports);
    }

    @Override
    public String toString() {
        return "ProcessPaymentReports{" +
                "reports=" + reports +
                '}';
    }

}
