package com.abcs.haiwaigou.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abcs.haiwaigou.adapter.SearchHistoryAdapter2;
import com.abcs.haiwaigou.adapter.SearchHotHisAdapter2;
import com.abcs.haiwaigou.model.Goods;
import com.abcs.hqbtravel.wedgt.MyGridView;
import com.abcs.hqbtravel.wedgt.MyListView;
import com.abcs.huaqiaobang.model.BaseActivity;
import com.abcs.huaqiaobang.util.HttpRequest;
import com.abcs.huaqiaobang.util.HttpRevMsg;
import com.thinksns.sociax.thinksnsbase.utils.TLUrl;
import com.abcs.sociax.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GoodsSearchActivity2 extends BaseActivity implements View.OnClickListener {

    @InjectView(R.id.et_search)
    EditText etSearch;
    @InjectView(R.id.layout_search_bar)
    RelativeLayout layoutSearchBar;
    @InjectView(R.id.relative_back)
    RelativeLayout relativeBack;
    @InjectView(R.id.img_search)
    ImageView imgSearch;
    @InjectView(R.id.relative_search)
    RelativeLayout relativeSearch;
    @InjectView(R.id.relative_title)
    RelativeLayout relativeTitle;
    @InjectView(R.id.recyclerView)
    MyListView recyclerView;
    @InjectView(R.id.t_clear)
    TextView tClear;
    @InjectView(R.id.img_clear)
    ImageView imgClear;
    @InjectView(R.id.hotrecyclerView)
    MyGridView hotrecyclerView;

    SearchHistoryAdapter2 searchHistoryAdapter;
    boolean isReSearch;
    SearchHotHisAdapter2 searchHotHisAdapter;
    private Handler handler=new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hwg_activity_goods_search2);
        ButterKnife.inject(this);
        isReSearch = getIntent().getBooleanExtra("reSearch", false);
        Log.i("zjz", "isReSearch=" + isReSearch);
        if (isReSearch) {
            etSearch.setText(getIntent().getStringExtra("text"));
            etSearch.setSelection(etSearch.getText().toString().length());
        }
        setOnListener();
        initHotRecycler();
        initRecycler();
        watchSearch();
    }


    public void watchSearch() {
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // 先隐藏键盘
                    ((InputMethodManager) etSearch.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(GoodsSearchActivity2.this
                                            .getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    goForSearch();
                    return true;
                }
                return false;
            }
        });
    }

    private void initHotRecycler() {

        initHotDatas();

        hotrecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(GoodsSearchActivity2.this, AllGoodsActivity.class);
                intent.putExtra("search", true);
                intent.putExtra("search_keyword", searchHotHisAdapter.getDates().get(i).getTitle());
                if (isReSearch) {
                    intent.putExtra("research",true);
                    setResult(1, intent);
                } else {
                    startActivity(intent);
                }
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(etSearch.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                finish();
            }
        });
    }


    private void initHotDatas() {
        HttpRequest.sendGet(TLUrl.getInstance().URL_hwg_hot_search, "act=hot_search&op=get_hot_search", new HttpRevMsg() {
            @Override
            public void revMsg(final String msg) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (msg.length() == 0) {
                            return;
                        }
                        try {
                            JSONObject jsonObject=new JSONObject(msg);
                            if(jsonObject.has("value")&& !TextUtils.isEmpty(jsonObject.optString("value"))){
                                String[] values = jsonObject.optString("value").split(",");

                                for (int i=0;i<values.length;i++){
                                    if(!TextUtils.isEmpty(values[i])){
                                        Log.e("zds","热门搜索==="+values[i]);

                                        Goods gods=new Goods();
                                        gods.setTitle(values[i]);
                                        goodsList.add(gods);
                                    }
                                }
                                searchHotHisAdapter=new SearchHotHisAdapter2(GoodsSearchActivity2.this,goodsList);
                                hotrecyclerView.setAdapter(searchHotHisAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    ArrayList<Goods> goodsList=new ArrayList<>();

    ArrayList<String>historys=new ArrayList<>();

    private void initRecycler() {
        SharedPreferences sp = getSharedPreferences("history_strs", 0);
        String save_history = sp.getString("history", "");
        String[] hisArrays = save_history.split(",");
        for (int i = hisArrays.length - 1; i > -1; i--) {
            if (!TextUtils.isEmpty(hisArrays[i])){

                Log.e("zds","history"+hisArrays[i]);
                historys.add(hisArrays[i]);

            }
        }
        searchHistoryAdapter = new SearchHistoryAdapter2(this, historys);
        recyclerView.setAdapter(searchHistoryAdapter);

        tClear.setVisibility(searchHistoryAdapter.getDatas().size() == 0 ? View.GONE : View.VISIBLE);
        Log.i("zjz", "history=" + searchHistoryAdapter.getDatas().size());

        recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                etSearch.setText(searchHistoryAdapter.getDatas().get(i));
                etSearch.setSelection(searchHistoryAdapter.getDatas().get(i).length());

                Intent intent = new Intent(GoodsSearchActivity2.this, AllGoodsActivity.class);
                intent.putExtra("search", true);
                intent.putExtra("search_keyword", searchHistoryAdapter.getDatas().get(i));
                if (isReSearch) {
                    intent.putExtra("research",true);
                    setResult(1, intent);
                } else {
                    startActivity(intent);
                }

                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(etSearch.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                finish();
            }
        });
    }

    private void setOnListener() {
        imgClear.setOnClickListener(this);
        tClear.setOnClickListener(this);
        relativeBack.setOnClickListener(this);
        relativeSearch.setOnClickListener(this);
    }


    private void Clear() {
        SharedPreferences sp = getSharedPreferences("history_strs", 0);
        sp.edit().clear().commit();
    }

    private void Save() {
        String text = etSearch.getText().toString();
        SharedPreferences sp = getSharedPreferences("history_strs", 0);
        String save_Str = sp.getString("history", "");
        String[] hisArrays = save_Str.split(",");
        for (int i = 0; i < hisArrays.length; i++) {
            if (hisArrays[i].equals(text)) {
                return;
            }
        }
        StringBuilder sb = new StringBuilder(save_Str);
        sb.append(text + ",");
        sp.edit().putString("history", sb.toString()).commit();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.relative_back:
                finish();
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(etSearch.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                break;

            case R.id.relative_search:
                goForSearch();
                break;
            case R.id.t_clear:

                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(etSearch.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                searchHistoryAdapter.clearDatas();
                Clear();
                initRecycler();
                break;
            case R.id.img_clear:
                etSearch.getText().clear();
                break;
        }
    }

    private void goForSearch(){
        Intent intent;
        if (TextUtils.isEmpty(etSearch.getText().toString())) {
            showToast("请输入搜索的商品信息");
        } else {
            intent = new Intent(this, AllGoodsActivity.class);
            intent.putExtra("search", true);
            intent.putExtra("search_keyword", etSearch.getText().toString());
            if (isReSearch) {
                intent.putExtra("research", true);
                setResult(1, intent);
            } else {
                startActivity(intent);
            }
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(etSearch.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            Save();
            finish();
        }
    }

}
