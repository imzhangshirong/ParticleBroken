package com.zhangshirong.particlebroken;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by jarvis on 17-8-17.
 */

public class ParticleBroken {
    private static HashMap<View,Boolean> brokingViews = new HashMap<View,Boolean>();
    /**
     * 粒子破碎效果
     *
     * @param brokenView    需要粒子破碎的View或者ViewGroup
     * @param listener   破碎结束监听
     * @param hideSelf   是否隐藏brokenView
     * @param hideChildren  是否隐藏brokenView子元素，brokenView必须为ViewGroup
     * @param removeChildren   是否在结束的时候移除brokenView子元素，brokenView必须为ViewGroup
     * @param cellspacing   粒子间隔大小
     * @param maxRadius   粒子最大半径
     * @param maxV   粒子最大移动速度
     * @param duration   粒子破碎时间
     */
    public static void broken(View brokenView, final ParticleBrokenListener listener, boolean hideSelf, boolean hideChildren, boolean removeChildren, int cellspacing, int maxRadius, int maxV, int duration){
        if(brokingViews.containsKey(brokenView))return;
        brokingViews.put(brokenView,true);
        final ParticleBrokenView view = new ParticleBrokenView(brokenView.getContext());
        final ViewGroup root = (ViewGroup) brokenView.getRootView();
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(MATCH_PARENT,MATCH_PARENT);
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setLayoutParams(lp);
        view.setTargetView(brokenView);
        root.addView(view);
        view.setConfig(cellspacing,maxRadius,maxV,duration);
        view.fadeStart(new ParticleBrokenListener() {
            @Override
            public void particleBrokenEnd(final View viewBroken) {
                brokingViews.remove(viewBroken);
                root.post(new Runnable() {
                    @Override
                    public void run() {
                        root.removeView(view);
                        listener.particleBrokenEnd(viewBroken);
                    }
                });
            }
        },hideSelf, hideChildren, removeChildren);
    }
}
