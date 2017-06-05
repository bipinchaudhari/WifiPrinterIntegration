package com.bipiinchowdhury.wifiprinterintegration;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import crl.android.pdfwriter.PDFWriter;
import crl.android.pdfwriter.PaperSize;
import crl.android.pdfwriter.StandardFonts;
import crl.android.pdfwriter.Transformation;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity {
    public static int white = 0xFFFFFFFF;
    public static int black = 0xFF000000;
    public final static int WIDTH=300;
    TextView mText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mText = (TextView) findViewById(R.id.tv_hello_word);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Generate QR Code", Snackbar.LENGTH_LONG)
                        .setAction("YES", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try
                                {
                                    Bitmap bmp =  encodeAsBitmap("non sense");
                                    ImageView imageView = (ImageView)findViewById(R.id.iv_qr_code);
                                    imageView.setImageBitmap(bmp);
                                    String pdfcontent = generatePDF(bmp);
                                    outputToScreen(mText, pdfcontent);
                                    outputToFile("helloworld.pdf", pdfcontent, "ISO-8859-1");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, WIDTH, 0, 0, w, h);
        return bitmap;
    }
    private String generatePDF(Bitmap bitmapQRCode){
        AssetManager mngr = getAssets();
        PDFWriter mPDFWriter = new PDFWriter(PaperSize.FOLIO_HEIGHT, PaperSize.FOLIO_WIDTH);
        try {

            mPDFWriter.addImageKeepRatio(0,0,PaperSize.FOLIO_HEIGHT,PaperSize.FOLIO_WIDTH,BitmapFactory.decodeStream(mngr.open("header_footer.png")));
            mPDFWriter.addImage(10,130,bitmapQRCode);
            //mPDFWriter.addLine(450,280,300,280);
            mPDFWriter.addImage(300,260,BitmapFactory.decodeStream(mngr.open("horizontal_bar.png")));
            mPDFWriter.setFont(StandardFonts.TIMES_BOLD, StandardFonts.TIMES_BOLD, StandardFonts.WIN_ANSI_ENCODING);
            mPDFWriter.addText(300,290,60,"Claudia Moro");
            mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.COURIER, StandardFonts.WIN_ANSI_ENCODING);
            mPDFWriter.addText(300,210,50,"SYSTEMAX ITALY SRL");

        } catch (IOException e) {
            e.printStackTrace();
        }

        int pageCount = mPDFWriter.getPageCount();
        for (int i = 0; i < pageCount; i++) {
            mPDFWriter.setCurrentPage(i);
            mPDFWriter.addText(10, 10, 8, Integer.toString(i + 1) + " / " + Integer.toString(pageCount));
        }

        String s = mPDFWriter.asString();
        return s;
    }
    private void outputToScreen(TextView mText, String pdfContent) {
        this.mText = mText;
        //this.mText.setText(pdfContent);
    }

    private void outputToFile(String fileName, String pdfContent, String encoding) throws FileNotFoundException {
        File downloads = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
        if (!downloads.exists() && !downloads.mkdirs())
            throw new RuntimeException("Could not create download folder");

        File newFile = new File(downloads, fileName);
        Log.i("PDF", "Writing file to " + newFile);

        try {
            newFile.createNewFile();
            try {
                FileOutputStream pdfFile = new FileOutputStream(newFile);
                pdfFile.write(pdfContent.getBytes(encoding));
                pdfFile.close();
            } catch (FileNotFoundException e) {
                Log.w("PDF", e);
            }
        } catch (IOException e) {
            Log.w("PDF", e);
        }
    }
}
