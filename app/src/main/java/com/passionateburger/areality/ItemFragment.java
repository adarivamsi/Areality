package com.passionateburger.areality;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

/**
 * Created by adari on 3/11/2018.
 */

public class ItemFragment extends Fragment{

    private static final String OBJECT_KEY = "KEY";
    private String CategoryKEY;
    private ViewPager mViewPager;
    private ProgressBar mProgressView;
    private FirebaseRecyclerAdapter<FireBaseHelper.Objects, viewholder> mAdapter = null;
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    public static ItemFragment newInstance(String category) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putString(OBJECT_KEY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CategoryKEY = getArguments().getString(OBJECT_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            mViewPager = (ViewPager) getActivity().findViewById(R.id.container);
            mProgressView = (ProgressBar) getActivity().findViewById(R.id.progress);
            final Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            Query query = FireBaseHelper.Objects.Ref.orderByChild(FireBaseHelper.Objects.Table.Category.text).equalTo(CategoryKEY);
            showProgress(true);
            new FireBaseHelper.Objects().Where(FireBaseHelper.Objects.Table.Category, CategoryKEY, Data -> {
                if (Data.size() == 0) {
                    showProgress(false);
                } else {
                    mAdapter = new FirebaseRecyclerAdapter<FireBaseHelper.Objects, viewholder>(
                            FireBaseHelper.Objects.class, R.layout.fragment_item, viewholder.class, query) {
                        @Override
                        protected void populateViewHolder(viewholder viewHolder, FireBaseHelper.Objects model, int position) {
                            model.Findbykey(mAdapter.getRef(position).getKey(), Data -> {
                                viewHolder.mTitleView.setText(Data.name);
                                viewHolder.mCompanyView.setText(Data.companies.name);
                                viewHolder.mRateView.setText(getRate(Data.feedbacks));
                                Picasso.with(getContext()).load(Data.image_path).into(viewHolder.mImageView);
                                viewHolder.mView.setOnClickListener(v -> {
                                    Intent intent = new Intent(getActivity(), ModelActivity.class);
                                    intent.putExtra(OBJECT_KEY, Data.Key);
                                    startActivity(intent);
                                });
                                viewHolder.mImageButton.setOnClickListener(v -> {
                                    PopupMenu popup = new PopupMenu(context,viewHolder.mImageButton);
                                    MenuInflater inflater1 = popup.getMenuInflater();
                                    inflater1.inflate(R.menu.pop_menu, popup.getMenu());
                                    popup.setOnMenuItemClickListener(item -> {
                                        int id = item.getItemId();
                                        if (id == R.id.item_download) {

                                        } else if (id == R.id.item_favorite) {

                                        } else if (id == R.id.item_delete) {

                                        }
                                        return true;
                                    });
                                    popup.show();
                                });
                                if (mAdapter.getItemCount() - 1 == position) {
                                    showProgress(false);
                                }
                            });
                        }
                    };
                    recyclerView.setAdapter(mAdapter);
                }
            });

        }
        return view;
    }

    private String getRate(List<FireBaseHelper.Feedbacks> lst) {

        int sum = 0;

        for (FireBaseHelper.Feedbacks item : lst) {
            sum += Integer.parseInt(item.rate);
        }
        return String.format(Locale.ENGLISH, "%.1f", (double) sum / lst.size());
    }

    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public static class viewholder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mCompanyView;
        public final TextView mRateView;
        public final ImageView mImageView;
        public final ImageButton mImageButton;

        public viewholder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.item_title);
            mCompanyView = (TextView) view.findViewById(R.id.item_company);
            mRateView = (TextView) view.findViewById(R.id.item_rate);
            mImageView = (ImageView) view.findViewById(R.id.item_image);
            mImageButton = (ImageButton) view.findViewById(R.id.item_menu);
        }
    }

}
