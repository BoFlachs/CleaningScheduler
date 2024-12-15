package cleaningscheduler.exporter;

import cleaningscheduler.domain.ISchedule;
import cleaningscheduler.domain.IWeek;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import scala.jdk.javaapi.CollectionConverters;
import scala.runtime.AbstractFunction1;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;


public class Exporter {
    static public String scheduleToPrettyString(ISchedule schedule, int score) {
        StringBuilder stringBuilder = new StringBuilder("Schedule\n\n");

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate = schedule.createdAt().toString(formatter);

        stringBuilder.append("Created at: ").append(formattedDate).append("\n");
        stringBuilder.append("Score: ").append(score).append("\n\n");

        int largestTaskNameLength = CollectionConverters.asJava(schedule.weekList()).stream()
                .map(week -> Collections.max(week.getTaskAssignmentAsJava().keySet().stream()
                        .map(task -> task.name().length())
                        .toList()))
                .max(Integer::compareTo)
                .orElse(0);

        schedule.weekList().toStream().foreach(new AbstractFunction1<IWeek, Void>() {
            @Override
            public Void apply(IWeek week) {
                stringBuilder.append("Week ").append(week.weekNumber()).append(":\n");
                week.getTaskAssignmentAsJava().forEach((task, person) ->
                        stringBuilder.append("    ").append(task.name())
                                .append(" ".repeat(largestTaskNameLength - task.name().length()))
                                .append("    - ").append(person.name())
                                .append(", ").append(task.costs()).append(" Minutes\n")
                );
                stringBuilder.append("\n");
                return null;
            }
        });
        stringBuilder.setLength(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }

    // Most of this code is based on the code on https://www.vogella.com/tutorials/JavaPDF/article.html
    static public byte[] scheduleToPDF(ISchedule schedule, int score) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            Document document = new Document();

            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String formattedDate = schedule.createdAt().toString(formatter);

            PdfWriter.getInstance(document, byteArrayOutputStream);

            document.open();

            document.addTitle("Schedule Created At " + formattedDate);
            addScheduleInfo(document, formattedDate, score);
            addWeekLists(document, schedule);

            document.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private static final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static final Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

    private static void addScheduleInfo(Document document, String formattedDate, int score) throws DocumentException {
        Paragraph scheduleInfo = new Paragraph();
        addEmptyLine(scheduleInfo, 1);
        scheduleInfo.add(new Paragraph("Schedule", catFont));

        addEmptyLine(scheduleInfo, 1);
        scheduleInfo.add(new Paragraph("Created At " + formattedDate, subFont));
        scheduleInfo.add(new Paragraph("Score: " + score, smallBold));
        addEmptyLine(scheduleInfo, 2);

        document.add(scheduleInfo);
    }

    private static void addWeekLists(Document document, ISchedule schedule) throws DocumentException {
        Paragraph weekLists = new Paragraph();
        PdfPTable table = new PdfPTable(4);
        table.getDefaultCell().setPadding(6f);

        PdfPCell c1 = new PdfPCell();
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Task"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Person"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Costs"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        table.setHeaderRows(1);

        int numberOfWeeks = schedule.weekList().length();
        AtomicInteger currentIndex = new AtomicInteger(1);
        schedule.weekList().toStream().foreach(new AbstractFunction1<IWeek, Void>() {
            @Override
            public Void apply(IWeek week) {
                table.addCell("Week " + week.weekNumber());
                table.addCell("");
                table.addCell("");
                table.addCell("");
                week.getTaskAssignmentAsJava().forEach((task, person) -> {
                            table.addCell("");
                            table.addCell(task.name());
                            table.addCell(person.name());
                            table.addCell(task.costs() + " minutes");
                        }
                );
                if (currentIndex.getAndIncrement() < numberOfWeeks) {
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell(" ");
                }
                return null;
            }
        });

        weekLists.add(table);
        document.add(weekLists);
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
