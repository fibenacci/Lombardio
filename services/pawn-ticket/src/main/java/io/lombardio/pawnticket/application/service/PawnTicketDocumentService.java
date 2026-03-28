/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.pawnticket.application.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class PawnTicketDocumentService {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
  private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
  private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
  private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8);

  public byte[] render(PawnTicket pawnTicket) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      Document document = new Document(PageSize.A4, 42, 42, 42, 42);
      PdfWriter writer = PdfWriter.getInstance(document, outputStream);
      document.open();

      document.add(title("Pfandschein"));
      document.add(spacer(8));
      document.add(summaryTable(pawnTicket));
      document.add(spacer(8));
      document.add(barcodeImage(writer, pawnTicket.contractBarcode()));
      document.add(spacer(12));
      document.add(section("Kundendaten"));
      document.add(
          keyValueTable(
              new String[][] {
                {"Kundennummer", pawnTicket.customerNumber()},
                {"Name", pawnTicket.customerDisplayName()},
                {"Telefon", blankFallback(pawnTicket.customerPhone())},
                {"Vertragsnummer", pawnTicket.contractNumber()},
                {"AGB-Version", pawnTicket.termsVersion()}
              }));
      document.add(spacer(12));
      document.add(section("Pfandgegenstaende"));
      document.add(positionTable(pawnTicket));
      document.add(spacer(12));
      document.add(section("Kosten und Fristen"));
      document.add(
          keyValueTable(
              new String[][] {
                {
                  "Ausstellungsdatum",
                  DATE_FORMAT.format(pawnTicket.createdAt().atZone(ZoneOffset.UTC).toLocalDate())
                },
                {"Faelligkeit", DATE_FORMAT.format(pawnTicket.dueDate())},
                {"Frueheste Versteigerung", DATE_FORMAT.format(pawnTicket.earliestAuctionDate())},
                {"Darlehensbetrag", formatCurrency(pawnTicket.loanAmount())},
                {
                  "Monatszins",
                  pawnTicket.monthlyInterestRate().stripTrailingZeros().toPlainString() + " %"
                },
                {"Betriebsverguetung / Monat", formatCurrency(pawnTicket.monthlyOperatingFee())},
                {"Zinsen gesamt", formatCurrency(pawnTicket.totalInterestAmount())},
                {"Betriebsverguetung gesamt", formatCurrency(pawnTicket.totalOperatingFeeAmount())},
                {"Rueckzahlungsbetrag", formatCurrency(pawnTicket.totalRepaymentAmount())}
              }));
      document.add(spacer(12));
      document.add(section("Rechtshinweise"));
      Paragraph legal = new Paragraph(pawnTicket.legalText(), BODY_FONT);
      legal.setLeading(14);
      document.add(legal);
      document.add(spacer(12));
      document.add(section("Geschaeftsbedingungen"));
      Paragraph terms = new Paragraph(pawnTicket.termsAndConditionsText(), BODY_FONT);
      terms.setLeading(14);
      document.add(terms);
      document.add(spacer(18));

      Paragraph signature = new Paragraph();
      signature.setFont(BODY_FONT);
      signature.add(
          new Chunk("Unterschrift Kundin/Kunde: _______________________________\n\n", BODY_FONT));
      signature.add(
          new Chunk("Unterschrift Pfandleiher/in: _______________________________", BODY_FONT));
      document.add(signature);

      document.close();
      return outputStream.toByteArray();
    } catch (DocumentException exception) {
      throw new IllegalStateException("Pawn ticket PDF could not be generated", exception);
    }
  }

  public byte[] renderLabels(PawnTicket pawnTicket) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      Document document = new Document(PageSize.A6.rotate(), 24, 24, 24, 24);
      PdfWriter writer = PdfWriter.getInstance(document, outputStream);
      document.open();

      for (int index = 0; index < pawnTicket.positions().size(); index++) {
        PawnTicketPosition position = pawnTicket.positions().get(index);
        if (index > 0) {
          document.newPage();
        }
        Paragraph header = new Paragraph("Pfandetikett", TITLE_FONT);
        header.setSpacingAfter(12);
        document.add(header);
        document.add(new Paragraph("Vertrag " + pawnTicket.contractNumber(), SECTION_FONT));
        document.add(spacer(6));
        document.add(barcodeImage(writer, position.itemBarcode()));
        document.add(spacer(8));
        document.add(new Paragraph("Pfandstueck " + position.itemNumber(), SECTION_FONT));
        document.add(new Paragraph(position.label(), BODY_FONT));
        document.add(new Paragraph(position.description(), BODY_FONT));
        document.add(
            new Paragraph("Beleihwert " + formatCurrency(position.pledgedValue()), BODY_FONT));
      }

      document.close();
      return outputStream.toByteArray();
    } catch (DocumentException exception) {
      throw new IllegalStateException("Pawn ticket labels could not be generated", exception);
    }
  }

  private Paragraph title(String value) {
    Paragraph paragraph = new Paragraph(value, TITLE_FONT);
    paragraph.setAlignment(Element.ALIGN_LEFT);
    return paragraph;
  }

  private Paragraph section(String value) {
    Paragraph paragraph = new Paragraph(value, SECTION_FONT);
    paragraph.setSpacingAfter(6);
    return paragraph;
  }

  private Paragraph spacer(float spacingAfter) {
    Paragraph paragraph = new Paragraph(" ", BODY_FONT);
    paragraph.setSpacingAfter(spacingAfter);
    return paragraph;
  }

  private PdfPTable summaryTable(PawnTicket pawnTicket) {
    PdfPTable table = new PdfPTable(new float[] {1.2f, 1.2f, 1.3f, 1.1f});
    table.setWidthPercentage(100);
    table.addCell(headerCell("Pfandschein-Nr."));
    table.addCell(headerCell("Vertrags-Nr."));
    table.addCell(headerCell("Mandant"));
    table.addCell(headerCell("Laufzeit"));
    table.addCell(valueCell(pawnTicket.ticketNumber()));
    table.addCell(valueCell(pawnTicket.contractNumber()));
    table.addCell(valueCell(pawnTicket.tenantId()));
    table.addCell(valueCell(pawnTicket.termMonths() + " Monate"));
    return table;
  }

  private PdfPTable keyValueTable(String[][] rows) {
    PdfPTable table = new PdfPTable(new float[] {1.2f, 2.2f});
    table.setWidthPercentage(100);
    for (String[] row : rows) {
      table.addCell(headerCell(row[0]));
      table.addCell(valueCell(row[1]));
    }
    return table;
  }

  private PdfPTable positionTable(PawnTicket pawnTicket) {
    PdfPTable table = new PdfPTable(new float[] {1.6f, 1.6f, 3.1f, 1.1f});
    table.setWidthPercentage(100);
    table.addCell(headerCell("Pfand-Nr."));
    table.addCell(headerCell("Bezeichnung"));
    table.addCell(headerCell("Beschreibung"));
    table.addCell(headerCell("Beleihwert"));

    for (PawnTicketPosition position : pawnTicket.positions()) {
      table.addCell(valueCell(position.itemNumber()));
      table.addCell(valueCell(position.label()));
      table.addCell(valueCell(position.description()));
      table.addCell(valueCell(formatCurrency(position.pledgedValue())));
    }
    return table;
  }

  private Image barcodeImage(PdfWriter writer, String barcodeValue) {
    Barcode128 barcode = new Barcode128();
    barcode.setCode(barcodeValue);
    barcode.setBarHeight(28);
    barcode.setX(0.9f);
    Image image = barcode.createImageWithBarcode(writer.getDirectContent(), null, null);
    image.setAlignment(Element.ALIGN_LEFT);
    return image;
  }

  private PdfPCell headerCell(String value) {
    PdfPCell cell = new PdfPCell(new Phrase(value, SECTION_FONT));
    cell.setBackgroundColor(new java.awt.Color(234, 239, 247));
    cell.setPadding(8);
    cell.setBorder(Rectangle.BOX);
    return cell;
  }

  private PdfPCell valueCell(String value) {
    PdfPCell cell = new PdfPCell(new Phrase(blankFallback(value), BODY_FONT));
    cell.setPadding(8);
    cell.setBorder(Rectangle.BOX);
    return cell;
  }

  private String formatCurrency(BigDecimal amount) {
    return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " EUR";
  }

  private String blankFallback(String value) {
    return value == null || value.isBlank() ? "-" : value;
  }
}
