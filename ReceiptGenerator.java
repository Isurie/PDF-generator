package com.testpackage.services.transaction.engine;

import com.testpackage.services.transaction.fundtransfers.db.model.FundTransferTransaction;
import com.testpackage.services.transaction.model.TransferStateHolder;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class ReceiptGenerator {

    @Value("${fund.transfer.receipt.download.folder}")
    private String receiptDownloadPath;

    @Value("${bank.receipt.companylogo.path}")
    String companyLogoPath;


    public String generateReceipt(FundTransferTransaction fundTransferTransaction, TransferStateHolder transferStateHolder){
        String downloadPath = "";
        String filePath = "";

        try {
            downloadPath = receiptDownloadPath;
            log.info("receiptDownloadPath:"+receiptDownloadPath);

            final String timestamp = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
            filePath = downloadPath + "Fund_Transfer"+"_"+fundTransferTransaction.getAux_no()+"_"+timestamp+".pdf";
            log.info("filePath --> "+ filePath);

            // Calculate the required height
            int numberOfDetails = 10; // Number of details to be printed
            float rowHeight = 20f; // Approximate height of each row
            float logoHeight = 100f; // Height of the logo
            float headerHeight = 40f; // Height of the header
            float totalHeight = logoHeight + headerHeight + (numberOfDetails * rowHeight) + 50f; // Adding some margin

            // Create a custom page size
            Rectangle pageSize = new Rectangle(PageSize.A4.getWidth(), totalHeight);
            Document document = new Document(pageSize);

            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            Font catFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 20, BaseColor.BLACK);
            Font font = FontFactory.getFont(FontFactory.COURIER, 13, BaseColor.BLACK);
            Font hFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);

            // Add the logo image and date in a table
            String logoPath = companyLogoPath + "Companylogo.png";
            log.info("companyLogoPath:"+companyLogoPath);
            Image logo = Image.getInstance(logoPath);
            logo.scaleToFit(150, 100);

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new int[]{1, 4});

            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            headerTable.addCell(logoCell);

            // Create a cell for date and receipt title
            Paragraph dateParagraph = new Paragraph(String.valueOf(new Date()), hFont);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            Paragraph titleParagraph = new Paragraph("Payment Receipt", catFont);
            titleParagraph.setAlignment(Element.ALIGN_RIGHT);

            // Combine date and title into a single cell
            PdfPCell dateTitleCell = new PdfPCell();
            dateTitleCell.addElement(dateParagraph);
            dateTitleCell.addElement(titleParagraph);
            dateTitleCell.setBorder(Rectangle.NO_BORDER);
            dateTitleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            headerTable.addCell(dateTitleCell);

            document.add(headerTable);

            // Add details with titles left-aligned and data right-aligned
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(20f);

            addDetailRow(detailsTable, "Aux No:", fundTransferTransaction.getAux_no(), font);
            //set Tran types
            String tranType = "";
            if(fundTransferTransaction.getTnx_type().equals("1")){ tranType = "Own Account Fund transfer";}
            if(fundTransferTransaction.getTnx_type().equals("2")){ tranType = "Third party Fund transfer";}
            if(fundTransferTransaction.getTnx_type().equals("3")){ tranType = "Inter bank Fund transfer";}
            if(fundTransferTransaction.getTnx_type().equals("4")){ tranType = "Mobile Fund transfer";}
            if(fundTransferTransaction.getTnx_type().equals("5")){ tranType = "Overseas Fund transfer";}
            addDetailRow(detailsTable, "Transaction Type:", tranType, font);
            addDetailRow(detailsTable, "From Account:", fundTransferTransaction.getFr_acc_id(), font);
            addDetailRow(detailsTable, "", fundTransferTransaction.getFr_acc_lbl(), font);
            addDetailRow(detailsTable, "To Account:", fundTransferTransaction.getToAccountId(), font);
            addDetailRow(detailsTable, "", fundTransferTransaction.getTo_acc_lbl(), font);
            String data_14 = fundTransferTransaction.getAccountCurr() + "_" + fundTransferTransaction.getOriginal_amt();
            addDetailRow(detailsTable, "Transaction Amount:", data_14, font);
            addDetailRow(detailsTable, "Narration:", fundTransferTransaction.getNarrat() != null ? fundTransferTransaction.getNarrat() : "N/A", font);
            addDetailRow(detailsTable, "Transaction Date:", String.valueOf(fundTransferTransaction.getAdded_date()), font);
            addDetailRow(detailsTable, "Fund Transfer Status:", transferStateHolder.getTransferStates().getMessage(), font);

            document.add(detailsTable);
            document.close();

            return Paths.get(filePath).getFileName().toString();

        } catch (DocumentException e) {
            log.info("DocumentException --> ");
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            log.info("FileNotFoundException --> ");
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void addDetailRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

}
