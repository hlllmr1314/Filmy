package tech.salroid.filmy.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tech.salroid.filmy.BuildConfig;
import tech.salroid.filmy.R;
import tech.salroid.filmy.activities.CharacterDetailsActivity;
import tech.salroid.filmy.activities.MovieDetailsActivity;
import tech.salroid.filmy.custom_adapter.SearchResultAdapter;
import tech.salroid.filmy.customs.BreathingProgress;
import tech.salroid.filmy.data_classes.SearchData;
import tech.salroid.filmy.network_stuff.VolleySingleton;
import tech.salroid.filmy.parser.SearchResultParseWork;
/*
 * Filmy Application for Android
 * Copyright (c) 2016 Ramankit Singh (http://github.com/webianks).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class SearchFragment extends Fragment implements SearchResultAdapter.ClickListener {


    SearchResultAdapter sadapter;

    @BindView((R.id.search_results_recycler))
    RecyclerView recycler;
    @BindView(R.id.breathingProgress)
    BreathingProgress breathingProgress;
    @BindView(R.id.fragment_rl)
    RelativeLayout fragmentRelativeLayout;
    private String api_key = BuildConfig.API_KEY;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean nightMode = sp.getBoolean("dark", false);


        if (nightMode)
            fragmentRelativeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.black));
        else
            fragmentRelativeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.grey));

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);

        if (tabletSize) {

            if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

                StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(6,
                        StaggeredGridLayoutManager.VERTICAL);
                recycler.setLayoutManager(gridLayoutManager);
            } else {
                StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(8,
                        StaggeredGridLayoutManager.VERTICAL);
                recycler.setLayoutManager(gridLayoutManager);
            }

        } else {

            if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

                StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3,
                        StaggeredGridLayoutManager.VERTICAL);
                recycler.setLayoutManager(gridLayoutManager);
            } else {
                StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(5,
                        StaggeredGridLayoutManager.VERTICAL);
                recycler.setLayoutManager(gridLayoutManager);
            }

        }

        return view;
    }





    @Override
    public void itemClicked(SearchData setterGetter, int position) {

        Intent intent;
        if (setterGetter.getType().equals("person"))
            intent = new Intent(getActivity(), CharacterDetailsActivity.class);
        else {
            intent = new Intent(getActivity(), MovieDetailsActivity.class);
            intent.putExtra("network_applicable", true);

        }
        intent.putExtra("title",setterGetter.getMovie());
        intent.putExtra("id", setterGetter.getId());
        intent.putExtra("activity", false);

        startActivity(intent);
    }


    public void getSearchedResult(String query) {


        String trimmedQuery = query.trim();
        String finalQuery = trimmedQuery.replace(" ", "-");


        VolleySingleton volleySingleton = VolleySingleton.getInstance();
        final RequestQueue requestQueue = volleySingleton.getRequestQueue();

        final String BASE_URL = "https://api.themoviedb.org/3/search/movie?api_key="+api_key+"&query="+finalQuery;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, BASE_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                      //  Log.d("webi", response.toString());
                        parseSearchedOutput(response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //Log.e("webi", "Volley Error: " + error.getCause());

            }
        }
        );

        requestQueue.add(jsonObjectRequest);


    }

    private void parseSearchedOutput(String s) {

        SearchResultParseWork park = new SearchResultParseWork(getActivity(), s);
        List<SearchData> list = park.parsesearchdata();
        sadapter = new SearchResultAdapter(getActivity(), list);
        recycler.setAdapter(sadapter);
        sadapter.setClickListener(this);

        hideProgress();

    }


    public void showProgress() {


        if (breathingProgress != null && recycler != null) {

            breathingProgress.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.INVISIBLE);

        }
    }


    public void hideProgress() {

        if (breathingProgress != null && recycler != null) {

            breathingProgress.setVisibility(View.INVISIBLE);
            recycler.setVisibility(View.VISIBLE);

        }
    }
}