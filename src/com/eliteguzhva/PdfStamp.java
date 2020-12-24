package com.eliteguzhva;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PdfStamp {
    private static final double MM_PER_INCH = 25.4;
    private PDDocument _pdf;

    public void init(String originalFile) throws IOException
    {
        _pdf = PDDocument.load(new File(originalFile));
    }

    public void save(String outputFile) throws IOException
    {
        _pdf.save(new File("build/output/" + outputFile));
    }

    public void putStamp(String stampTemplate, String text,
                         double widthMM, double heightMM,
                         double offsetX, double offsetY, boolean useTopLeftOffset,
                         Color bgColor, Color fgColor) throws Exception
    {
        // get page
        int pageIndex = 0;
        PDPage page = _pdf.getPage(pageIndex);

        PDRectangle vRect = page.getBleedBox();
        double pageHeight = vRect.getHeight() / 72 * MM_PER_INCH;
        double pageWidth = vRect.getWidth() / 72 * MM_PER_INCH;

        if (pageWidth < widthMM || pageHeight < heightMM)
            throw new RuntimeException("Размеры штампа не должны превышать размеры страницы");

        // create page content stream
        PDPageContentStream contentStream = new PDPageContentStream(_pdf, page, PDPageContentStream.AppendMode.APPEND,
                true, true);

        // params
        int res = 300; //DPI по умолчанию
        int widthOrg  = (int)Math.round(res * widthMM / MM_PER_INCH);
        int heightOrg = (int)Math.round(res * heightMM / MM_PER_INCH);

        // read image to buffer
        BufferedImage sti = ImageIO.read(new File(stampTemplate));
        if (sti == null)
            throw new RuntimeException("Некорректный формат рисунка");

        // draw text
        Graphics2D stg = sti.createGraphics();
        stg.setColor(fgColor);
        Font font = new Font("Arial", Font.BOLD, 100);
        stg.setFont(font);
        FontMetrics fm = stg.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        stg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
        long posX = Math.round((float)(sti.getWidth() - textWidth) / 2.0);
        long posY = Math.round((float)sti.getHeight() / 4.0 - (float)textHeight / 2.0);
        stg.drawString(text, posX, posY);
        stg.dispose();

        // fill background
        BufferedImage image = new BufferedImage(widthOrg, heightOrg, BufferedImage.TYPE_INT_RGB);
        Graphics2D gi = image.createGraphics();
        gi.setColor(bgColor);
        gi.fillRect(0, 0, widthOrg, heightOrg);
        gi.drawImage(sti, 0, 0, widthOrg, heightOrg, null);
        gi.dispose();

        // write buffered image to byte stream
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", stream);
        byte[] imageContent = stream.toByteArray();

        // create pdf image instance
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(_pdf,
                imageContent, "stamp");

        // calculate position
        double x, y, w, h;
        h = heightMM;
        w = widthMM;

        if (useTopLeftOffset)
        {
            x = offsetX;
            y = offsetY + h;
        }
        else
        {
            x = pageWidth - w - offsetX;
            y = pageHeight - offsetY;
        }

        // add image to pdf
        Point2D point = getTranslatedCoords(x, y, pageHeight);
        contentStream.drawImage(pdImage, (float) point.getX(),
                (float) point.getY(), (float) mmTo1_72inch(w), (float) mmTo1_72inch(h));
        contentStream.close();
    }

    private static double mmTo1_72inch(double mm)
    {
        return mm / MM_PER_INCH * 72;
    }

    private static Point2D getTranslatedCoords(double x, double y, double deltaY)
    {
        return new Point2D.Double(mmTo1_72inch(x), mmTo1_72inch(deltaY - y));
    }
}
