package com.abcs.haiwaigou.local.huohang.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abcs.haiwaigou.adapter.GridLocalTagAdapter;
import com.abcs.haiwaigou.local.beans.ActivityArr;
import com.abcs.haiwaigou.local.beans.HuoHangItem;
import com.abcs.haiwaigou.local.beans.TagArr;
import com.abcs.haiwaigou.local.beans.localPaiXu2;
import com.abcs.haiwaigou.local.huohang.view.HuoHangFragment;
import com.abcs.huaqiaobang.MyApplication;
import com.abcs.sociax.android.R;
import com.abcs.sociax.t4.android.ActivityHome;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.thinksns.sociax.thinksnsbase.utils.TLUrl;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class PeiSongDetialAdapter3 extends RecyclerArrayAdapter<localPaiXu2.DatasEntry>{
    public PeiSongDetialAdapter3(Context context, TextView shopCart) {
        super(context);
        this.shopCart = shopCart;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new PeiSongDetialHoder3(parent);
    }


    public ImageView buyImg;
    public ViewGroup animMaskLayout;
    public static Map<String,Integer> goodsNumMap=new HashMap<>(); //用来item的购买数量
    public static Map<Integer,HuoHangItem> datas=new HashMap<>(); //用来保存添加到购物车的对象
    public static Map<Integer,Double> goodsPri=new HashMap<>(); //用来保存价格总数
    public int buyNum;
    public TextView shopCart,goods_price;
    public ActivityHome mActivity;
    public HuoHangFragment fragment;
    public Map<Integer, HuoHangItem> getDatas() {
        return datas;
    }
    public Map<Integer, Double> getGoodsPrice() {
        return goodsPri;
    }

    public void setmActivity(ActivityHome mActivity, HuoHangFragment fragment) {
        this.mActivity = mActivity;
        this.fragment = fragment;
    }

    public interface OnShopCartGoodsChangeListener {
        public void onNumChange(int num, HuoHangItem data);
    }

    public OnShopCartGoodsChangeListener mOnGoodsNunChangeListener = null;

    public void setOnShopCartGoodsChangeListener(OnShopCartGoodsChangeListener e){
        mOnGoodsNunChangeListener = e;
    }

    /**
     * 设置动画
     *
     * @param v              要显示的内容
     * @param start_location 坐标
     */
    public void setAnim(final View v, int[] start_location) {
        animMaskLayout = null;
        animMaskLayout = createAnimLayout();
        animMaskLayout.addView(v);// 把动画小球添加到动画层
        final View view = addViewToAnimLayout(animMaskLayout, v,
                start_location);
        int[] end_location = new int[2];// 这是用来存储动画结束位置的X、Y坐标
        shopCart.getLocationInWindow(end_location);// shopCart是那个购物车
        // 计算位移
        int endX = 0 - start_location[0] + 40;// 动画位移的X坐标
        int endY = end_location[1] - start_location[1];// 动画位移的y坐标
        TranslateAnimation translateAnimationX = new TranslateAnimation(0,
                endX, 0, 0);
        translateAnimationX.setInterpolator(new LinearInterpolator());
        translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationX.setFillAfter(true);
        TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0,
                0, endY);
        translateAnimationY.setInterpolator(new AccelerateInterpolator());
        translateAnimationY.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationX.setFillAfter(true);
        AnimationSet set = new AnimationSet(false);
        set.setFillAfter(false);
        set.addAnimation(translateAnimationY);
        set.addAnimation(translateAnimationX);
        set.setDuration(500);// 动画的执行时间
        view.startAnimation(set);
        // 动画监听事件
        set.setAnimationListener(new Animation.AnimationListener() {
            // 动画的开始
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }
            // 动画的结束
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 修改购物车状态
     */
    public void changeShopCart(int buyNum) {

        Log.i("zds", "changeShopCart: buyNum"+buyNum);
        if (buyNum > 0) {
//            shopCart.setVisibility(View.VISIBLE);
            if(fragment.cart_num>0){
                shopCart.setText((buyNum +fragment.cart_num)+ "");
            }else {
                shopCart.setText(buyNum + "");
            }

            ////
//            fragment.tv_peisong_number.setVisibility(View.VISIBLE);
            fragment.img_peisong_che.setImageResource(R.drawable.bg_bottom_psong_che2);
            fragment.tv_peisong_qisong.setText("选好了");
            fragment.re_peisong_jie.setBackgroundResource(R.drawable.bg_bottom_psong_jie);

        } else {
            if(fragment.cart_num>0){
                shopCart.setText(fragment.cart_num+ "");
                ////
//                fragment.tv_peisong_number.setVisibility(View.VISIBLE);
                fragment.img_peisong_che.setImageResource(R.drawable.bg_bottom_psong_che2);
                fragment.tv_peisong_qisong.setText("选好了");
                fragment.re_peisong_jie.setBackgroundResource(R.drawable.bg_bottom_psong_jie);

            }else {
//                shopCart.setVisibility(View.GONE);
                ///
                fragment.tv_peisong_gfw.setText("购物车为空");
//                fragment.tv_peisong_number.setVisibility(View.GONE);
                fragment.img_peisong_che.setImageResource(R.drawable.bg_bottom_psong_che2);
                fragment.tv_peisong_qisong.setText("€0.00");
                fragment.re_peisong_jie.setBackgroundResource(R.drawable.bg_bottom_psong_jie_no);
            }
        }
    }

    /**
     * 初始化动画图层
     *
     * @return
     */
    public ViewGroup createAnimLayout() {
        ViewGroup rootView = (ViewGroup) mActivity.getWindow().getDecorView();
        LinearLayout animLayout = new LinearLayout(mActivity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }


    /**
     * 将View添加到动画图层
     *
     * @param vg
     * @param view
     * @param location
     * @return
     */
    public View addViewToAnimLayout(final ViewGroup vg, final View view,int[] location) {
        int x = location[0];
        int y = location[1];
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }


    /**
     * Created by Administrator on 2016/9/8.
     */
    public class PeiSongDetialHoder3 extends BaseViewHolder<localPaiXu2.DatasEntry>{

        public TextView tv_ping;
        View line;
        LinearLayout liner_content;

        public PeiSongDetialHoder3(ViewGroup parent) {
            super(parent, R.layout.item_local_peisong_pin);

            liner_content=(LinearLayout) itemView.findViewById(R.id.liner_content);
            tv_ping=(TextView) itemView.findViewById(R.id.tv_ping);
            line=(View) itemView.findViewById(R.id.line);

        }

        @Override
        public void setData(final localPaiXu2.DatasEntry item) {

            if(item!=null){
                Log.i("zds", "setData: item.goodsLetter"+item.goodsLetter);

                if(!TextUtils.isEmpty(item.goodsLetter)){
                    tv_ping.setText(item.goodsLetter);
                    tv_ping.setVisibility(View.VISIBLE);
                }else {
                    tv_ping.setVisibility(View.GONE);
                    line.setVisibility(View.GONE);
                }

                liner_content.removeAllViews();
                if(item.goodsList!=null){

                    if(item.goodsList.size()>0){
                        liner_content.setVisibility(View.VISIBLE);
                        for (int i = 0; i <item.goodsList.size() ; i++) {

                            final HuoHangItem data=item.goodsList.get(i);

                            View itemView=View.inflate(getContext(),R.layout.item_local_peisong2,null);

                            ViewGroup parent = (ViewGroup) itemView.getParent();
                            if (parent != null) {
                                parent.removeAllViews();
                            }

                            ImageView img_logo=(ImageView) itemView.findViewById(R.id.img_logo);
                            LinearLayout lin_tianjai=(LinearLayout) itemView.findViewById(R.id.lin_tianjai);
                            LinearLayout liner_hdong=(LinearLayout) itemView.findViewById(R.id.liner_hdong);
                            GridView liner_tag=(GridView) itemView.findViewById(R.id.liner_tag);
//                            LinearLayout liner_tag=(LinearLayout) itemView.findViewById(R.id.liner_tag);
                            TextView tv_en_title=(TextView) itemView.findViewById(R.id.tv_en_title);
                            TextView tv_title=(TextView) itemView.findViewById(R.id.tv_title);
                            TextView tv_store=(TextView) itemView.findViewById(R.id.tv_store);
                            TextView tv_price=(TextView) itemView.findViewById(R.id.tv_price);
                            TextView tv_sui=(TextView) itemView.findViewById(R.id.tv_sui);

                            final ImageView ivGoodsMinus = (ImageView) itemView.findViewById(R.id.ivGoodsMinus);
                            final TextView tvGoodsSelectNum = (TextView) itemView.findViewById(R.id.tvGoodsSelectNum);
                            final ImageView ivGoodsAdd = (ImageView) itemView.findViewById(R.id.ivGoodsAdd);

                            if(i==item.goodsList.size()-1){
                                line.setVisibility(View.GONE);
                            }else {
                                line.setVisibility(View.VISIBLE);
                            }

                            if(data!=null){

                                liner_hdong.removeAllViews();
//                                liner_tag.removeAllViews();

                                lin_tianjai.setVisibility(View.VISIBLE);
                                if(!TextUtils.isEmpty(data.goodsImage)){
//                http://www.huaqiaobang.com/data/upload/shop/store/goods/11/11_05375327290881075.jpg
                                    MyApplication.imageLoader.displayImage(TLUrl.getInstance().URL_hwg_base+"/data/upload/shop/store/goods/11/"+data.goodsImage,img_logo,MyApplication.getCircleImageOptions());
                                }
                                tv_title.setText(data.goodsName+"");
                                tv_en_title.setText(data.goodsEnName+"");
                                tv_store.setText("货号:"+data.goodsSerial);
                                tv_price.setText("€"+data.goodsPrice);
                                tv_sui.setText(data.tax_rate+"");

                                /*活动信息*/
                                if(data.activityArr!=null){

                                    if(data.activityArr.size()>0){
                                        liner_hdong.setVisibility(View.VISIBLE);
                                        for (int k = 0; k <data.activityArr.size() ; k++) {

                                            ActivityArr bean_hd=data.activityArr.get(k);
                                            View view=View.inflate(getContext(),R.layout.item_hd_text2,null);

                                            ViewGroup parent3 = (ViewGroup) view.getParent();
                                            if (parent3 != null) {
                                                parent3.removeAllViews();
                                            }

                                            ImageView img=(ImageView) view.findViewById(R.id.img);
                                            TextView tv=(TextView) view.findViewById(R.id.tv);

                                            if(bean_hd!=null){
                                                if(!TextUtils.isEmpty(bean_hd.img)){
//                http://www.huaqiaobang.com/data/upload/shop/store/goods/11/11_05375327290881075.jpg
                                                    MyApplication.imageLoader.displayImage(bean_hd.img,img,MyApplication.options);
                                                }

                                                if(!TextUtils.isEmpty(bean_hd.activityName)){
                                                    tv.setText(bean_hd.activityName);
                                                }
                                            }

                                            liner_hdong.addView(view);
                                        }
                                    }else {
                                        liner_hdong.setVisibility(View.GONE);
                                    }
                                }

                                /*tag标签*/
                                if(data.tagArr!=null){

                                    if(data.tagArr.size()>0){

                                        final GridLocalTagAdapter tagadapter=new GridLocalTagAdapter(mActivity,data.tagArr);
                                        liner_tag.setAdapter(tagadapter);
                                        liner_tag.setVisibility(View.VISIBLE);

                                        liner_tag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                TagArr bean_hd =tagadapter.getItem(position);
                                                Log.i("zds", "onItemClick: "+bean_hd.tagName);
                                                fragment.addTopLineTag(bean_hd);
                                            }
                                        });
                                    }else {
                                        liner_tag.setVisibility(View.GONE);
                                    }
                                }

//                                判断商品是否有添加到购物车中

                                if (!goodsNumMap.containsKey(data.goodsId) ||goodsNumMap.get(data.goodsId)  == 0) {
                                    tvGoodsSelectNum.setVisibility(View.GONE);
                                    ivGoodsMinus.setVisibility(View.GONE);
                                } else {
                                    tvGoodsSelectNum.setVisibility(View.VISIBLE);
                                    tvGoodsSelectNum.setText(goodsNumMap.get(data.goodsId)+ "");
                                    ivGoodsMinus.setVisibility(View.VISIBLE);
                                }


                                if(fragment.is_LocalMember==1){ // 不是货行用户
                                    ivGoodsAdd.setVisibility(View.GONE);
                                    if(ivGoodsMinus.getVisibility()==View.VISIBLE){
                                        ivGoodsMinus.setVisibility(View.GONE);
                                    }

                                    if(tvGoodsSelectNum.getVisibility()==View.VISIBLE){
                                        tvGoodsSelectNum.setVisibility(View.GONE);
                                    }


                                    if(data.price_hide==1){  // 隐藏价格
                                        tv_price.setVisibility(View.GONE);
                                        tv_sui.setVisibility(View.GONE);
                                    }else {

                                        tv_price.setVisibility(View.VISIBLE);
                                        tv_sui.setVisibility(View.VISIBLE);
                                    }
                                }else {
                                    ivGoodsAdd.setVisibility(View.VISIBLE);
                                    tv_price.setVisibility(View.VISIBLE);
                                    tv_sui.setVisibility(View.VISIBLE);
                                }

                                ivGoodsAdd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        int goodsNum = 0;
                                        if (goodsNumMap.containsKey(data.goodsId)){
                                            goodsNum = goodsNumMap.get(data.goodsId);
                                        }
                                        goodsNum++;
                                        goodsNumMap.put(data.goodsId,goodsNum);
                                        // TODO:  添加购物车
                                        int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
                                        v.getLocationInWindow(start_location);// 这是获取购买按钮的在屏幕的X、Y坐标（这也是动画开始的坐标）
                                        buyImg = new ImageView(getContext());
                                        buyImg.setBackgroundResource(R.drawable.icon_goods_add_item);// 设置buyImg的图片
                                        buyNum++;
//                                        setAnim(buyImg, start_location);// 开始执行动画
                                        changeShopCart(buyNum);
                                        data.numbers=goodsNum;

                                        if (goodsNum == 0) {
                                            tvGoodsSelectNum.setVisibility(View.GONE);
                                            ivGoodsMinus.setVisibility(View.GONE);
                                            datas.remove(Integer.valueOf(data.goodsId));
                                            goodsPri.remove(Integer.valueOf(data.goodsId));
                                        } else {
                                            tvGoodsSelectNum.setVisibility(View.VISIBLE);
                                            tvGoodsSelectNum.setText(goodsNum + "");
                                            ivGoodsMinus.setVisibility(View.VISIBLE);
                                            datas.put(Integer.valueOf(data.goodsId),data);
                                            goodsPri.put(Integer.valueOf(data.goodsId),data.goodsPrice*goodsNum);
                                        }

                                        mOnGoodsNunChangeListener.onNumChange(buyNum,data);

                                    }
                                });
                                ivGoodsMinus.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        int goodsNum = 0;
                                        if (goodsNumMap.containsKey(data.goodsId)){
                                            goodsNum = goodsNumMap.get(data.goodsId);
                                        }

                                        if (goodsNum > 0) {
                                            goodsNum--;
                                            buyNum--;
                                            changeShopCart(buyNum);
                                            data.numbers=goodsNum;
                                            goodsNumMap.put(data.goodsId,goodsNum);
                                            //  删除购物车内容

                                            if (goodsNum == 0) {
                                                tvGoodsSelectNum.setVisibility(View.GONE);
                                                ivGoodsMinus.setVisibility(View.GONE);
                                                datas.remove(Integer.valueOf(data.goodsId));
                                                goodsPri.remove(Integer.valueOf(data.goodsId));
                                            } else {
                                                tvGoodsSelectNum.setVisibility(View.VISIBLE);
                                                tvGoodsSelectNum.setText(goodsNum + "");
                                                ivGoodsMinus.setVisibility(View.VISIBLE);
                                                datas.put(Integer.valueOf(data.goodsId),data);
                                                goodsPri.put(Integer.valueOf(data.goodsId),data.goodsPrice*goodsNum);
                                            }

                                            mOnGoodsNunChangeListener.onNumChange(buyNum,data);
                                        }
                                    }
                                });
                            }
                            liner_content.addView(itemView);
                        }
                    }else {
                        tv_ping.setVisibility(View.GONE);
                        liner_content.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}
