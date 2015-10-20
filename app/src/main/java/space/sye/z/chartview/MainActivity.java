package space.sye.z.chartview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import space.sye.z.library.ChartView;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> ordinateList = new ArrayList<>();
    private ArrayList<String> abscissaList = new ArrayList<>();
    private ArrayList<Float> historgramList = new ArrayList<>();
    private HashMap<Integer, ArrayList<Float>> brokenLineMap = new HashMap<>();
    private ChartView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chartView = (ChartView) findViewById(R.id.chartView);
        for (int i = 0; i < 4; i++) {
            ordinateList.add(i * 1000 + "");
        }
        for (int i = 0; i < 8; i++) {
            abscissaList.add("05-2" + i);
        }
        for (int i = 0; i < 8; i++) {
            historgramList.add((float) (2634 - i * 200));
        }

        ArrayList<Float> brokenLine;
        for(int i = 0; i < 3; i++){
            brokenLine = new ArrayList<>();
            for (int j = 0; j < 8; j++){
                brokenLine.add(1345f + i * 200 + j * 40);
                brokenLineMap.put(i, brokenLine);
            }
        }

        chartView.setAbscissa(abscissaList);
        chartView.setOrdinate(ordinateList);
        chartView.setHistorgramList(historgramList);
        chartView.setBrokenLineMap(brokenLineMap);
        chartView.onSettingFinished();

        chartView.setOnTouchListener(new ChartView.OnInsideTouchListener() {

            @Override
            public void show() {
                Toast.makeText(MainActivity.this, "当前按下的是第" + chartView.getSelectPosition() + "个", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void dismiss() {
            }
        });
    }


}
