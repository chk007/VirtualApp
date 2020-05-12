package io.virtualapp.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.virtualapp.R;
import io.virtualapp.VCommends;
import io.virtualapp.abs.ui.VFragment;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.adapters.CloneAppListAdapter;
import io.virtualapp.home.adapters.decorations.ItemOffsetDecoration;
import io.virtualapp.home.models.AppInfo;
import io.virtualapp.home.models.AppInfoLite;
import io.virtualapp.widgets.DragSelectRecyclerView;

/**
 * VApp安装页面
 * @author Lody
 */
public class ListAppFragment extends VFragment<ListAppContract.ListAppPresenter> implements ListAppContract.ListAppView {
    private static final String KEY_SELECT_FROM = "key_select_from";
    private DragSelectRecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private Button mInstallButton;
    private CloneAppListAdapter mAdapter;

    public static ListAppFragment newInstance(File selectFrom) {
        Bundle args = new Bundle();
        if (selectFrom != null)
            args.putString(KEY_SELECT_FROM, selectFrom.getPath());
        ListAppFragment fragment = new ListAppFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private File getSelectFrom() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String selectFrom = bundle.getString(KEY_SELECT_FROM);
            if (selectFrom != null) {
                return new File(selectFrom);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_app, null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.saveInstanceState(outState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mRecyclerView = (DragSelectRecyclerView) view.findViewById(R.id.select_app_recycler_view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.select_app_progress_bar);
        mInstallButton = (Button) view.findViewById(R.id.select_app_install_btn);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL));
        mRecyclerView.addItemDecoration(new ItemOffsetDecoration(VUiKit.dpToPx(getContext(), 2)));
        mAdapter = new CloneAppListAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new CloneAppListAdapter.ItemEventListener() {
            @Override
            public void onItemClick(AppInfo info, int position) {
                int count = mAdapter.getSelectedCount();
                if (!mAdapter.isIndexSelected(position)) {
                    if (count >= 9) {
                        Toast.makeText(getContext(), R.string.install_too_much_once_time, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                mAdapter.toggleSelected(position);
            }

            @Override
            public boolean isSelectable(int position) {
                return mAdapter.isIndexSelected(position) || mAdapter.getSelectedCount() < 9;
            }
        });
        mAdapter.setSelectionListener(count -> {
            mInstallButton.setEnabled(count > 0);
            mInstallButton.setText(String.format(Locale.ENGLISH, getResources().getString(R.string.install_d), count));
        });
        // 安装按钮点击事件：触发安装操作
        mInstallButton.setOnClickListener(v -> {
            Integer[] selectedIndices = mAdapter.getSelectedIndices();
            ArrayList<AppInfoLite> dataList = new ArrayList<AppInfoLite>(selectedIndices.length);
            for (int index : selectedIndices) {
                AppInfo info = mAdapter.getItem(index);
                dataList.add(new AppInfoLite(info.packageName, info.path, info.fastOpen));
            }
            /**
             * 1. 结束当前的ListAppActivity，并通过setResult接口通知HomeActivity处理后续Install流程;
             * 2. setResult只是将resultCode和resultData设置到当前ListAppActivity之中
             * 3. getActivity().finish()操作通知AMS结束当前进程，并将StartActivity Result传递给启动当前Activity的HomeActivity
             * 4. AMS通过向HomeActivity所在的ActivityThread发送SEND_RESULT消息;
             * 5. ActivityThread.handleSendResult最终会调用Activity.dispatchActivityResult将消息分发给HomeActivity，
             *    进而调用HomeActivity.onActivityResult处理消息
              */
            Intent data = new Intent();
            data.putParcelableArrayListExtra(VCommends.EXTRA_APP_INFO_LIST, dataList);
            getActivity().setResult(Activity.RESULT_OK, data);
            getActivity().finish();
        });
        new ListAppPresenterImpl(getActivity(), this, getSelectFrom()).start();
    }

    @Override
    public void startLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void loadFinish(List<AppInfo> infoList) {
        mAdapter.setList(infoList);
        mRecyclerView.setDragSelectActive(false, 0);
        mAdapter.setSelected(0, false);
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPresenter(ListAppContract.ListAppPresenter presenter) {
        this.mPresenter = presenter;
    }

}
