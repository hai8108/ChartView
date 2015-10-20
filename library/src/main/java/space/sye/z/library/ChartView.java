package space.sye.z.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sye on 2015/10/15.
 */
public class ChartView extends View {

    private Context context;
    /**
     * x轴距离底部的距离
     */
    private float xAxisMarginBottom;
    /**
     * x轴字体大小
     */
    private float xAxisTextSize;
    /**
     * y轴字体大小
     */
    private float yAxisTextSize;
    /**
     * 是否显示柱状图
     */
    private boolean histogramShow;
    /**
     * 是否显示折线图
     */
    private boolean brokenLineShow;
    /**
     * 柱状图的颜色
     */
    private int historgramColor;
    private Paint xAxisPaint;
    private Paint yAxisPaint;
    private Paint histogramPaint;
    private Paint linePaint;
    /**
     * y轴文本高度
     */
    private float ordinateTextHeight;
    private int chartViewWidth;
    private int chartViewHeight;

    /**
     * 纵坐标数值list
     */
    private ArrayList<String> ordinateList = new ArrayList<>();
    private ArrayList<String> abscissaList = new ArrayList<>();
    private ArrayList<Float> historgramList = new ArrayList<>();
    private HashMap<Integer, ArrayList<Float>> brokenLineMap = new HashMap<>();
    private HashMap<Integer, ArrayList<Circle>> circleListMap = new HashMap<>();

    private int ordinateSize;
    private float spaceY;
    private float ordinateTextWidth;
    private int abscissaSize;
    private float spaceX;
    private float abscissaTextWidth;
    private float yAxisHeight;
    private int[] colors;
    private float downX;
    private float downY;
    private OnInsideTouchListener listener;
    private boolean inside = false;
    private float histogramXStart;
    private float histogramYStart;
    private float histogramXEnd;
    private float histogramYEnd;
    private int selectPosition;


    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.chartView);
        xAxisMarginBottom = a.getDimension(R.styleable.chartView_xAxisMarginBottom, 50);
        xAxisTextSize = a.getDimension(R.styleable.chartView_xAxisTextSize, 22);
        yAxisTextSize = a.getDimension(R.styleable.chartView_yAxisTextSize, 25);
        histogramShow = a.getBoolean(R.styleable.chartView_histogramShow, true);
        historgramColor = a.getColor(R.styleable.chartView_historgramColor, context.getResources().getColor(R.color.chatview_green));
        brokenLineShow = a.getBoolean(R.styleable.chartView_brokenLineShow, true);
        a.recycle();

        //x轴画笔
        xAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        xAxisPaint.setTextSize(xAxisTextSize);
        xAxisPaint.setColor(Color.BLACK);

        //y轴画笔
        yAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        yAxisPaint.setTextSize(yAxisTextSize);
        yAxisPaint.setColor(Color.BLACK);
        Paint.FontMetrics fontMetrics = yAxisPaint.getFontMetrics();
        float ascent = fontMetrics.ascent;
        float descent = fontMetrics.descent;
        ordinateTextHeight = Math.abs(ascent + descent);

        //柱状图画笔
        histogramPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        histogramPaint.setColor(historgramColor);
        histogramPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));

        //折线图画笔
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);

        colors = new int[]{context.getResources().getColor(R.color.chatview_deepgreen),
                context.getResources().getColor(R.color.chatview_red),
                context.getResources().getColor(R.color.chatview_yellow)};
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        chartViewWidth = width - getPaddingLeft() - getPaddingRight();
        chartViewHeight = height - getPaddingTop() - getPaddingBottom();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(drawBitmap(), 0, 0, null);
        super.onDraw(canvas);
    }

    private Bitmap drawBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(chartViewWidth, chartViewHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        drawOrdinate(canvas);

        drawOrdinateLine(canvas);

        drawAbscissaLine(canvas);

        drawAbscissa(canvas);

        if (histogramShow) {
            drawHistogram(canvas);
        }

        if (brokenLineShow) {
            drawBrokenLine(canvas);
        }

        return bitmap;
    }

    /**
     * 画折线图
     *
     * @param canvas
     */
    private void drawBrokenLine(Canvas canvas) {
        if (0 == abscissaSize || 0 == brokenLineMap.size()) {
            return;
        }

        //画折线图的圆
        linePaint.setStrokeWidth(3);
        ArrayList<Circle> circleList;
        for (int i = 0; i < brokenLineMap.size(); i++) {
            circleList = new ArrayList<>();
            linePaint.setColor(colors[i]);
            for (int j = 0; j < abscissaSize; j++) {

                //获得需要画的圆的纵坐标
                float brokenLineYAxis = chartViewHeight - xAxisMarginBottom - (brokenLineMap.get(i).get(j) / 3000f) * yAxisHeight;
                canvas.drawCircle(ordinateTextWidth + 30 + j * spaceX + abscissaTextWidth / 2, brokenLineYAxis, 10, linePaint);

                Circle circle = new Circle(ordinateTextWidth + 30 + j * spaceX + abscissaTextWidth / 2, brokenLineYAxis, 10);
                circleList.add(circle);
                circleListMap.put(i, circleList);
            }
        }

        //画折线图的连线
        linePaint.setStrokeWidth(1);
        for (int i = 0; i < circleListMap.size(); i++) {
            linePaint.setColor(colors[i]);
            for (int j = 1; j < circleListMap.get(i).size(); j++) {
                drawLineBetweenCirCLe(canvas, circleListMap.get(i).get(j - 1).getX(),
                        circleListMap.get(i).get(j - 1).getY(),
                        circleListMap.get(i).get(j - 1).getR(),
                        circleListMap.get(i).get(j).getX(),
                        circleListMap.get(i).get(j).getY(),
                        circleListMap.get(i).get(j).getR());
            }
        }
    }

    private void drawLineBetweenCirCLe(Canvas canvas, float x1, float y1, float r1, float x2, float y2, float r2) {
        //直线方程 y = kx + b;
        float k = (y2 - y1) / (x2 - x1);
        float b = y1 - k * x1;

        float temp1 = (float) (x1 < x2 ? r1 / Math.sqrt(1 + k * k) : -r1 / Math.sqrt(1 + k * k));
        float a1 = x1 + temp1;
        float b1 = k * a1 + b;

        float temp2 = (float) (x1 < x2 ? r2 / Math.sqrt(1 + k * k) : -r2 / Math.sqrt(1 + k * k));
        float a2 = x2 - temp2;
        float b2 = k * a2 + b;

        canvas.drawLine(a1, b1, a2, b2, linePaint);
    }

    private class Circle {

        private float x;
        private float y;
        private float r;

        public Circle(float x, float y, float r) {
            this.x = x;
            this.y = y;
            this.r = r;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getR() {
            return r;
        }
    }

    /**
     * 画柱状图
     *
     * @param canvas
     */
    private void drawHistogram(Canvas canvas) {
        if (abscissaSize == 0) {
            return;
        }
        yAxisHeight = chartViewHeight - xAxisMarginBottom - ordinateTextHeight / 2;
        for (int i = 0; i < abscissaSize; i++) {
            float historgramHeight = chartViewHeight - xAxisMarginBottom - (historgramList.get(i) / 3000f) * yAxisHeight;
            float left = ordinateTextWidth + 30 + i * spaceX + xAxisTextSize / 2.5f;
            float top = historgramHeight;
            float right = ordinateTextWidth + 30 + i * spaceX + abscissaTextWidth - xAxisTextSize / 2.5f;
            float bottom = chartViewHeight - xAxisMarginBottom - ordinateTextHeight / 2;
            canvas.drawRect(left, top, right, bottom, histogramPaint);
        }
    }

    /**
     * 画横坐标文本
     *
     * @param canvas
     */
    private void drawAbscissa(Canvas canvas) {
        abscissaSize = abscissaList.size();
        if (abscissaSize == 0) {
            return;
        }
        //横坐标文本间距
        spaceX = (chartViewWidth - ordinateTextWidth - 30) / abscissaSize;
        abscissaTextWidth = 0;
        for (int i = 0; i < abscissaSize; i++) {
            canvas.drawText(abscissaList.get(i), ordinateTextWidth + 30 + i * spaceX, chartViewHeight - 15, xAxisPaint);
            float abscissaTextWidthTemp = xAxisPaint.measureText(abscissaList.get(i));
            if (abscissaTextWidth < abscissaTextWidthTemp) {
                abscissaTextWidth = abscissaTextWidthTemp;
            }
        }
    }

    /**
     * 画横坐标线
     *
     * @param canvas
     */
    private void drawAbscissaLine(Canvas canvas) {
        if (ordinateSize == 0) {
            return;
        }
        for (int i = 0; i < ordinateSize; i++) {
            canvas.drawLine(ordinateTextWidth + 10,
                    chartViewHeight - i * spaceY - i * ordinateTextHeight - ordinateTextHeight / 2 - xAxisMarginBottom,
                    chartViewWidth,
                    chartViewHeight - i * spaceY - i * ordinateTextHeight - ordinateTextHeight / 2 - xAxisMarginBottom,
                    xAxisPaint);
        }
    }

    /**
     * 画纵坐标线
     *
     * @param canvas
     */
    private void drawOrdinateLine(Canvas canvas) {
        if (ordinateSize == 0) {
            return;
        }
        canvas.drawLine(ordinateTextWidth + 10,
                chartViewHeight - (ordinateSize - 1) * spaceY - (ordinateSize - 1) * ordinateTextHeight - ordinateTextHeight / 2 - xAxisMarginBottom,
                ordinateTextWidth + 10,
                chartViewHeight - xAxisMarginBottom - ordinateTextHeight / 2, yAxisPaint);
    }

    /**
     * 画纵坐标文本
     *
     * @param canvas
     */
    private void drawOrdinate(Canvas canvas) {
        ordinateSize = ordinateList.size();
        if (0 == ordinateSize) {
            return;
        }

        //计算y轴文本间距
        spaceY = (chartViewHeight - xAxisMarginBottom - ordinateSize * ordinateTextHeight) / (ordinateSize - 1);
        //y轴方向文本宽度
        ordinateTextWidth = 0;
        yAxisPaint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 1; i < ordinateSize; i++) {
            //循环遍历数组，获得字符的最大长度，作为绘制字符的起点
            float ordinateTextWidthTemp = yAxisPaint.measureText(ordinateList.get(i));
            if (ordinateTextWidth < ordinateTextWidthTemp) {
                ordinateTextWidth = ordinateTextWidthTemp;
            }
        }

        for (int i = 0; i < ordinateSize; i++){
            canvas.drawText(ordinateList.get(i), ordinateTextWidth,
                    chartViewHeight - i * spaceY - i * ordinateTextHeight - xAxisMarginBottom + 2,
                    yAxisPaint);
        }
    }

    /**
     * 设置纵坐标
     *
     * @param ordinateList
     */
    public void setOrdinate(ArrayList<String> ordinateList) {
        this.ordinateList.clear();
        this.ordinateList.addAll(ordinateList);
    }

    /**
     * 设置纵坐标
     *
     * @param abscissaList
     */
    public void setAbscissa(ArrayList<String> abscissaList) {
        this.abscissaList.clear();
        this.abscissaList.addAll(abscissaList);
    }

    /**
     * 设置柱状图的纵坐标
     *
     * @param historgramList
     */
    public void setHistorgramList(ArrayList<Float> historgramList) {
        this.historgramList.clear();
        this.historgramList.addAll(historgramList);
    }

    /**
     * 设置折线图的点坐标map
     *
     * @param brokenLineMap
     */
    public void setBrokenLineMap(Map<Integer, ArrayList<Float>> brokenLineMap) {
        this.brokenLineMap.clear();
        this.brokenLineMap.putAll(brokenLineMap);
    }

    public void onSettingFinished() {
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                inside = isInside(downX, downY);
                if (inside) {
                    if (null != listener) {
                        listener.show();
                    }
                } else {
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (inside) {
                    if (null != listener) {
                        listener.dismiss();
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 判断点是否在柱状图内
     *
     * @param downX
     * @param downY
     * @return
     */
    private boolean isInside(float downX, float downY) {

        for (int i = 0; i < abscissaSize; i++) {
            histogramXStart = ordinateTextWidth + 30 + i * spaceX + xAxisTextSize / 3;
            histogramYStart = chartViewHeight - xAxisMarginBottom - (historgramList.get(i) / 3000f) * yAxisHeight;
            histogramXEnd = ordinateTextWidth + 30 + i * spaceX + abscissaTextWidth - xAxisTextSize / 3;
            histogramYEnd = chartViewHeight - xAxisMarginBottom - ordinateTextHeight / 2;
            if (downX >= histogramXStart && downX <= histogramXEnd && downY >= histogramYStart && downY <= histogramYEnd) {
                selectPosition = i;
                return true;
            }
        }
        return false;
    }

    public interface OnInsideTouchListener {
        void show();

        void dismiss();
    }

    public void setOnTouchListener(OnInsideTouchListener listener) {
        this.listener = listener;
    }

    /**
     * @return x轴纵坐标
     */
    public float getXAxis() {
        return chartViewHeight - xAxisMarginBottom - ordinateTextHeight / 2;
    }

    /**
     * @return y轴最高点纵坐标
     */
    public float getYAxis() {
        return ordinateTextHeight / 2 - 1;
    }

    /**
     * @return 选择的是哪个柱状图
     */
    public int getSelectPosition() {
        return selectPosition;
    }

    /**
     * @return 被选中的柱状图的起始横坐标
     */
    public float getHistogramXStart() {
        return histogramXStart;
    }

    /**
     * @return 被选中的柱状图的终点横坐标
     */
    public float getHistogramXEnd() {
        return histogramXEnd;
    }

    /**
     * @return 被选中的柱状图的终点纵坐标
     */
    public float getHistogramYEnd() {
        return histogramYEnd;
    }

    /**
     * @return 手指按下点的横坐标
     */
    public float getDownX() {
        return downX;
    }

    /**
     * @return 手指按下点的纵坐标
     */
    public float getDownY() {
        return downY;
    }
}
