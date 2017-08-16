package com.zhangshirong.particlebroken;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jarvis on 17-8-16.
 */

public class ParticleBrokenView extends View {
    private int particelCellRadius = 10;//取色间隔，越小粒子数量越多
    private float particelMinRadius = 1;
    private float particelMaxRadius = 4;//粒子最大半径
    private int duration = 500;//破碎时间
    private float particelMaxV = 10;//最大移动速度
    private Bitmap fadeImage;
    private Canvas fadeCanvas;
    private Bitmap m_fadeImage;//缓冲
    private Canvas m_fadeCanvas;//缓冲
    private Paint mpaint = new Paint();
    private View targetView;
    private int[] basePosition = new int[]{0,0};
    Particle[] particles;


    public ParticleBrokenView(Context context) {
        super(context);
    }

    public ParticleBrokenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParticleBrokenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTargetView(View targetView){
        this.targetView = targetView;
    }

    public void setConfig(int cellspacing, int maxRadius, int maxV, int duration){
        particelCellRadius = cellspacing;
        particelMaxRadius = maxRadius;
        particelMaxV = maxV;
        this.duration = duration;
        if(this.duration < 0)this.duration = 0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(fadeImage!=null){
            setMeasuredDimension(fadeImage.getWidth(),
                    fadeImage.getHeight());
        }
        else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
    private Bitmap loadBitmapFromView(View v) {
        if (v == null) {
            return null;
        }
        Bitmap screenshot;
        if(v.getWidth() == 0 || v.getHeight() == 0){
            return null;
        }
        screenshot = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(screenshot);
        c.translate(-v.getScrollX(), -v.getScrollY());
        v.draw(c);
        return screenshot;
    }
    public void fadeStart(final ParticleBrokenListener call, boolean hideSelf, boolean hideChildren, final boolean removeChildren){
        if(fadeImage != null)return;
        if(targetView == null)return;
        Bitmap cache = loadBitmapFromView(targetView);
        if(cache == null){
            if(call != null)call.particleBrokenEnd(targetView);
            return;
        }
        int width = cache.getWidth();
        int height = cache.getHeight();
        View root = targetView.getRootView();
        int windowsWidth = root.getWidth();
        int windowsHeight = root.getHeight();
        fadeImage = Bitmap.createBitmap(windowsWidth, windowsHeight, Bitmap.Config.ARGB_4444);
        fadeCanvas = new Canvas(fadeImage);
        m_fadeImage = Bitmap.createBitmap(windowsWidth, windowsHeight, Bitmap.Config.ARGB_4444);
        m_fadeCanvas = new Canvas(m_fadeImage);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        setBackgroundColor(Color.TRANSPARENT);
        float dp = getResources().getDisplayMetrics().density;
        int cellRadius = (int) (dp * particelCellRadius);
        int maxRadius = (int) (dp * particelMaxRadius);
        int minRadius = (int) (dp * particelMinRadius);
        float maxV = dp * particelMaxV;
        int[] pixels = new int[width*height];
        int x = 0;
        int y = 0;
        Random random = new Random(System.currentTimeMillis());
        float[] border = new float[]{maxRadius * 2,maxRadius * 2,width - maxRadius * 2,height - maxRadius * 2};
        ArrayList<Particle> particlesList = new ArrayList<Particle>();
        //final int frameCount = (int)duration / 20;
        for(int i = 0;i < pixels.length;i++){
            if(x > border[0] && y > border[1] && x < border[2] && y < border[3]){
                int color = cache.getPixel(x,y);
                float r = random.nextFloat() * (maxRadius - minRadius) + minRadius;
                int xm = (int) ((2 * random.nextFloat() - 1) * maxRadius) + x;
                int ym = (int) ((2 * random.nextFloat() - 1) * maxRadius) + y;
                if(xm < 0)xm = 0;
                if(ym < 0)ym = 0;
                if(xm > width)ym = (int) (border[2] - 1);
                if(ym > height)ym = (int) (border[3] - 1);
                Particle particle = new Particle(
                        xm,
                        ym,
                        color,
                        1,
                        r,
                        random.nextFloat()*maxV,
                        new PVector((2 * random.nextFloat() - 1),(2 * random.nextFloat() - 1))
                );
                particlesList.add(particle);
            }
            x += cellRadius;
            if(x >= width){
                x = 0;
                y += cellRadius;
            }
            if(y >= height)break;

        }
        cache.recycle();
        particles = new Particle[particlesList.size()];
        particlesList.toArray(particles);
        particlesList.clear();

        if(hideSelf)targetView.setAlpha(0);
        if(hideChildren || removeChildren){
            ViewGroup group = (ViewGroup)targetView;
            for(int i = 0;i<group.getChildCount();i++){
                View v = group.getChildAt(i);
                v.setAlpha(0);
            }
        }

        final long startTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                float progress = (1-(float)(System.currentTimeMillis()-startTime)/duration);
                while(progress>0){
                    int alpha = (int) (progress*255);
                    targetView.getLocationInWindow(basePosition);
                    m_fadeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    for(int a = 0;a<particles.length;a++){
                        Particle particle = particles[a];
                        //particle.v += particle.dv*progress;
                        particle.x += particle.v*particle.pv.x;
                        particle.y += particle.v*particle.pv.y;
                        particle.alpha = alpha;
                        paint.setColor(particle.color);
                        paint.setAlpha((int) particle.alpha);
                        m_fadeCanvas.drawCircle(basePosition[0]+particle.x,basePosition[1]+particle.y,particle.r,paint);
                    }
                    fadeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    fadeCanvas.drawBitmap(m_fadeImage,0,0,mpaint);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progress = (1-(float)(System.currentTimeMillis()-startTime)/duration);
                }
                post(new Runnable() {
                    @Override
                    public void run() {
                        m_fadeImage.recycle();
                        m_fadeCanvas = null;
                        fadeImage.recycle();
                        fadeCanvas = null;
                        fadeImage = null;
                        particles = null;
                        if(removeChildren){
                            ((ViewGroup)targetView).removeAllViews();
                        }
                    }
                });
                if(call != null)call.particleBrokenEnd(targetView);
                //System.out.println("broken end! use:"+(System.currentTimeMillis()-startTime));
            }
        }).start();
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        if(fadeImage == null)super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(fadeImage!=null){
            canvas.drawBitmap(fadeImage,0,0,mpaint);
        }
        super.onDraw(canvas);
    }

    class Particle{
        public float x = 0;
        public float y = 0;
        public int color = 0;
        public float alpha = 0;
        public float r = 0;
        public float v = 0;//速度
        public PVector pv;
        public Particle(float x, float y, int color, float alpha, float r,float v, PVector pv){
            this.x = x;
            this.y = y;
            this.color = color;
            this.r = r;
            this.alpha = alpha;
            this.v = v;
            this.pv = pv;
        }
    }
    class PVector{
        public float x;
        public float y;
        public PVector(float x,float y){
            float d = (float) Math.sqrt(x*x+y*y);
            this.x = x / d;
            this.y = y / d;
        }
    }

}
