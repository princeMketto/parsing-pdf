package com.niafikra.pdfParser.spring;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.filter.TextRegionEventFilter;
import com.itextpdf.kernel.pdf.canvas.parser.listener.FilteredTextEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.layout.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Juma mketto
 * @mail jmketto@gmail.com
 * @Date 10/08/2018
 */
public class PDFUtil {
    private PdfWriter pdfWriter;
    private PdfDocument pdfDocument;
    private Document document;
    private LocationTextExtractionStrategy strategy;
    private PdfCanvasProcessor parser;
    private String filePath, fileSubject;
    private Rectangle rectangle;
    private float divergence = 0;
    private Random random = new Random();


    public PDFUtil(String filePath) throws IOException {
        this.filePath = filePath;
        pdfDocument = new PdfDocument(new PdfReader(filePath));

        strategy = new LocationTextExtractionStrategy();
        parser = new PdfCanvasProcessor(strategy);

        fileSubject = pdfDocument.getDocumentInfo().
                getTitle().
                replaceAll("[0-9]", "").
                replace("\\", "")
                .toLowerCase()
                .trim();

        try {
            checkFileOrigin();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void checkFileOrigin() throws IOException {
        divergence = Float.parseFloat(getDivergence(loadOrigin()));
    }

    private String getDivergence(Map<String, String> fileOrigin) {
        String value = fileOrigin.entrySet().stream()
                .filter(map -> isContainingKey(map))
                .map(map -> map.getValue())
                .collect(Collectors.joining()).trim();
        return !value.isEmpty() || value==null ? value : "0";
    }

    private boolean isContainingKey(Map.Entry<String, String> map) {
        if (fileSubject.contains(map.getKey()) || fileSubject.equals(map.getKey()))
            return true;
        else
            return false;
    }

    private Map<String, String> loadOrigin() throws IOException {
        Map<String, String> filesSubjects = new HashMap<>();
        String line;
        String settingFilePath = Paths.get(".").toAbsolutePath()
                .normalize().toString() + File.separator + "fileOrigin.txt";

        BufferedReader reader = new BufferedReader(new FileReader(settingFilePath));
        while ((line = reader.readLine()) != null) {
            if (line.trim().charAt(0) == '#') continue;

            String[] parts = line.split(":", 2);
            if (parts.length >= 2) {
                filesSubjects.put(parts[0].toLowerCase().trim(), parts[1].trim());
            }
        }
        return filesSubjects;
    }


    public String readAllText() {
        for (int page = 1; page <= pdfDocument.getNumberOfPages(); page++) {
            parser.processPageContent(pdfDocument.getPage(page));
        }
        return strategy.getResultantText();
    }

    public String readTextOnPosition(float x, float y, float width, float height) {

        float pdfHeight = pdfDocument.getFirstPage().getPageSize().getHeight();
        float itextY = pdfHeight - ((y - divergence) + height);

        TextRegionEventFilter regionEventFilter = new TextRegionEventFilter(
                new Rectangle(x, itextY, width - (divergence * 2), height));

        ITextExtractionStrategy strategy;
        String result = "";
        for (int page = 1; page <= pdfDocument.getNumberOfPages(); page++) {
            strategy = new FilteredTextEventListener(new LocationTextExtractionStrategy(), regionEventFilter);
            result += PdfTextExtractor.getTextFromPage(pdfDocument.getPage(page), strategy) + "\n";
        }
        return result;
    }
    public Map imagesFromDocument(){
        Map imageData = new HashMap();

        PdfDocumentContentParser contentParser = new PdfDocumentContentParser(pdfDocument);
        for (int page = 1; page <= pdfDocument.getNumberOfPages(); page++) {
            contentParser.processContent(page, getRenderListener(imageData));
        }

        return imageData;
    }

    private IEventListener getRenderListener(Map imageData) {
        return new IEventListener() {

            @Override
            public void eventOccurred(IEventData data, EventType type) {
                if (data instanceof ImageRenderInfo){
                    ImageRenderInfo imageRenderInfo = (ImageRenderInfo) data;
                    byte [] bytes = imageRenderInfo.getImage().getImageBytes();

                    imageData.put("Img_"+bytes.toString(),bytes);
                }

            }

            @Override
            public Set<EventType> getSupportedEvents() {
                return Collections.singleton(EventType.RENDER_IMAGE);
            }
        };
    }
}


