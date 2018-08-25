package com.niafikra.pdfParser.spring;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import org.junit.BeforeClass;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;

/**
 * The main view contains a simple label element and a template element.
 */
@HtmlImport("styles/shared-styles.html")
@Route
public class MainView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {
    public static final String SRC = "./src/main/resources/P2.pdf";
    public static final String DEST = "./src/main/resources/image/";

   // final static File RESULT_FOLDER = new File(DEST, "extract");
    private Button readFile, extract;
    private MultiFileMemoryBuffer multiFileMemoryBuffer;
    private FileReceiver receiver = new FileReceiver();
    private Upload upload;
    private PDFUtil pdfUtil;
    private TextArea textArea;
    private TextField x, y, width, height;
    private HorizontalLayout fieldsHolder = new HorizontalLayout();

    public MainView() {
        try {
            pdfUtil = new PDFUtil(SRC);
        } catch (IOException e) {
            e.printStackTrace();
        }

        upload = new Upload(new MultiFileMemoryBuffer());
        upload.setDropLabelIcon(new Icon(VaadinIcon.PIN));
        upload.setWidth("100%");
        upload.addSucceededListener(succeededEvent -> getFile(succeededEvent));

        extract = new Button("Extract");
        extract.setIcon(new Icon(VaadinIcon.ABACUS));
        extract.setWidth("100%");
        extract.addClickListener(this);

        readFile = new Button("Read File");
        readFile.setIcon(new Icon(VaadinIcon.OPEN_BOOK));
        readFile.setWidth("100%");
        readFile.addClickListener(this);

        textArea = new TextArea();
        textArea.setWidth("100%");
        textArea.setLabel("Text From document");


        x = new TextField("X");
        x.setWidth("50%");

        y = new TextField("y");
        y.setWidth("50%");

        width = new TextField("width");
        width.setWidth("50%");

        height = new TextField("height");
        height.setWidth("50%");

        fieldsHolder.add(x, y, width, height, extract);

        add(readFile);
        add(fieldsHolder);
        add(upload);
        add(textArea);
    }


    private void getFile(SucceededEvent event) {
        System.out.println(event.getSource() + "\n" + event.getMIMEType());

    }

    @Override
    public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {

        if (buttonClickEvent.getSource() == readFile) {
            textArea.setValue(pdfUtil.readAllText());
        }
        if (buttonClickEvent.getSource() == extract) {
            textArea.setValue(pdfUtil.readTextOnPosition(getX(), getY(), getX2(), getY2()));
            Map imageBytes = pdfUtil.imagesFromDocument();

            imageBytes.forEach((key, value) -> writeImage(key, value));

        }
    }

    private void writeImage(Object key, Object value) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream((byte[]) value));
            ImageIO.write(image,"jpg",new File(DEST+key.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private float getY2() {
        float end2 = Float.parseFloat(height.getValue());
        return end2;
    }

    private float getX2() {
        float end1 = Float.parseFloat(width.getValue());
        return end1;
    }

    private float getY() {
        float start2 = Float.parseFloat(y.getValue());
        return start2;
    }

    private float getX() {
        float start1 = Float.parseFloat(x.getValue());
        return start1;
    }

    public class FileReceiver implements Receiver {

        @Override
        public OutputStream receiveUpload(String s, String s1) {
            return null;
        }
    }
}
