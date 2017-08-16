package com.zhangshirong.particlebroken;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private LinearLayout particleContent;
    private Button buttonBreak;
    private Button buttonResume;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        particleContent = (LinearLayout) findViewById(R.id.particleContent);
        buttonBreak = (Button) findViewById(R.id.startParticle);
        buttonResume = (Button) findViewById(R.id.resumeButton);
        buttonResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = View.inflate(getBaseContext(),R.layout.item,null);
                particleContent.addView(view);
            }
        });
        buttonBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParticleBroken.broken(particleContent, new ParticleBrokenListener() {
                    @Override
                    public void particleBrokenEnd(View viewBroken) {
                        System.out.println("View broken end");
                    }
                },false,true,true,10,4,10,500);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
