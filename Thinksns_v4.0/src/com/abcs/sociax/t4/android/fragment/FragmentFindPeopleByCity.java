package com.abcs.sociax.t4.android.fragment;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.abcs.sociax.android.R;
import com.abcs.sociax.t4.adapter.AdapterFindPeopleByCity;
import com.abcs.sociax.t4.adapter.AdapterSociaxList;
import com.abcs.sociax.t4.android.Thinksns;
import com.abcs.sociax.t4.android.user.ActivityUserInfo_2;
import com.abcs.sociax.t4.model.ModelSearchUser;
import com.thinksns.sociax.thinksnsbase.bean.ListData;
import com.thinksns.sociax.thinksnsbase.bean.SociaxItem;

/**
 * 类说明： 地区用户
 *
 * @author wz
 * @version 1.0
 * @date 2014-10-30
 */
public class FragmentFindPeopleByCity extends FragmentSociax {
    protected int selectpostion;
    private PullToRefreshListView pullToRefreshListView;

    private int city_id;
    private LinearLayout title_layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        listView = pullToRefreshListView.getRefreshableView();
        listView.setDivider(new ColorDrawable(getResources().getColor(R.color.bg_ios)));
        listView.setDividerHeight(1);
        listView.setSelector(getResources().getDrawable(R.drawable.listitem_selector));

        adapter = createAdapter();
        listView.setAdapter(adapter);

        title_layout = (LinearLayout) findViewById(R.id.title_layout);

        title_layout.setVisibility(View.GONE);
    }

    @Override
    public AdapterSociaxList createAdapter() {
        list = new ListData<SociaxItem>();
        return new AdapterFindPeopleByCity(this, list, getActivity()
                .getIntent().getIntExtra("uid", Thinksns.getMy().getUid()),
                city_id);
    }

    @Override
    public void initIntentData() {
        if (getActivity().getIntent().hasExtra("city_id")) {
            city_id = getActivity().getIntent().getIntExtra("city_id", 0);
        } else {
            Toast.makeText(getActivity(), "操作失败", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public void initListener() {
        pullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (list.size() > 0) {
                    ModelSearchUser user = (ModelSearchUser) adapter.getItem((int) id);
                    Intent intent = new Intent(getActivity(), ActivityUserInfo_2.class);
                    intent.putExtra("uid", user.getUid());
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public View getDefaultView() {
        return findViewById(R.id.default_people_bg);
    }

    @Override
    public void initData() {
        adapter.loadInitData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_chat_userlist;
    }

    @Override
    public PullToRefreshListView getPullRefreshView() {
        return pullToRefreshListView;
    }
}
