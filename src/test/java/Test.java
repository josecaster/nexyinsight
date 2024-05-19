import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import sr.we.entity.eclipsestore.Database;


import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.aspose.words.*;

public class Test {
    public static void main(String[] args) throws Exception {
//        boolean instance = Database.class.isInstance(new Database());
//        System.out.println(instance);

        Document doc = new Document("C:\\Tools\\workspaces\\nexyinsight\\src\\test\\java\\input.docx");
        doc.save("C:\\Tools\\workspaces\\nexyinsight\\src\\test\\java\\output.pdf");

//        try {
//            // Load the Word document
//            FileInputStream fis = new FileInputStream("C:\\Tools\\workspaces\\nexyinsight\\src\\test\\java\\input.docx");
//            XWPFDocument document = new XWPFDocument(fis);
//
//            // Create PDF document
//            PDDocument pdfDocument = new PDDocument();
//            PDPage page = new PDPage();
//            pdfDocument.addPage(page);
//
//            // Write text from Word document to PDF
//            PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page);
//            contentStream.setFont(PDType1Font.HELVETICA, 12);
//            float y = 700; // Initial Y position
//            for (XWPFParagraph paragraph : document.getParagraphs()) {
//                float x = 100; // Initial X position
//                for (XWPFRun run : paragraph.getRuns()) {
//                    String text = run.getText(0);
//                    if (text != null) {
//                        contentStream.beginText();
//                        contentStream.newLineAtOffset(x, y);
//                        contentStream.showText(text);
//                        contentStream.endText();
//                        x += run.getTextPosition() / 1000 * 12; // Adjust for font size
//                    }
//                }
//                y -= 20; // Adjust for line spacing
//            }
//            contentStream.close();
//
//            // Save PDF document
//            FileOutputStream fos = new FileOutputStream("C:\\Tools\\workspaces\\nexyinsight\\src\\test\\java\\output.pdf");
//            pdfDocument.save(fos);
//            pdfDocument.close();
//
//            // Close streams
//            fis.close();
//            fos.close();
//
//            System.out.println("Word document converted to PDF successfully.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
