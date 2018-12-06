/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.applications.manageapplications;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;

import static com.android.settings.applications.manageapplications.AppFilterRegistry
        .FILTER_APPS_ALL;
import static com.android.settings.applications.manageapplications.ManageApplications
        .LIST_TYPE_MAIN;
import static com.android.settings.applications.manageapplications.ManageApplications
        .LIST_TYPE_NOTIFICATION;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.testutils.SettingsRobolectricTestRunner;
import com.android.settings.widget.LoadingViewController;
import com.android.settingslib.applications.ApplicationsState;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;

@RunWith(SettingsRobolectricTestRunner.class)
public class ManageApplicationsTest {

    @Mock
    private ApplicationsState mState;
    @Mock
    private ApplicationsState.Session mSession;
    @Mock
    private Menu mMenu;
    @Mock
    private FragmentActivity mActivity;
    @Mock
    private Resources mResources;
    @Mock
    private UserManager mUserManager;
    @Mock
    private PackageManager mPackageManager;
    private MenuItem mAppReset;
    private MenuItem mSortRecent;
    private MenuItem mSortFrequent;
    private ManageApplications mFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mAppReset = new RoboMenuItem(R.id.reset_app_preferences);
        mSortRecent = new RoboMenuItem(R.id.sort_order_recent_notification);
        mSortFrequent = new RoboMenuItem(R.id.sort_order_frequent_notification);
        ReflectionHelpers.setStaticField(ApplicationsState.class, "sInstance", mState);
        when(mState.newSession(any())).thenReturn(mSession);
        when(mState.getBackgroundLooper()).thenReturn(Looper.myLooper());

        mFragment = spy(new ManageApplications());
        when(mFragment.getActivity()).thenReturn(mActivity);
        when(mActivity.getResources()).thenReturn(mResources);
        when(mActivity.getSystemService(UserManager.class)).thenReturn(mUserManager);
        when(mActivity.getPackageManager()).thenReturn(mPackageManager);
    }

    @Test
    public void updateMenu_mainListType_showAppReset() {
        setUpOptionMenus();
        ReflectionHelpers.setField(mFragment, "mListType", LIST_TYPE_MAIN);
        ReflectionHelpers.setField(mFragment, "mOptionsMenu", mMenu);

        mFragment.updateOptionsMenu();
        assertThat(mMenu.findItem(R.id.reset_app_preferences).isVisible()).isTrue();
    }

    @Test
    public void updateMenu_batteryListType_hideAppReset() {
        setUpOptionMenus();
        ReflectionHelpers.setField(mFragment, "mListType", ManageApplications.LIST_TYPE_HIGH_POWER);
        ReflectionHelpers.setField(mFragment, "mOptionsMenu", mMenu);

        mFragment.updateOptionsMenu();
        assertThat(mMenu.findItem(R.id.reset_app_preferences).isVisible()).isFalse();
    }

    @Test
    public void updateMenu_hideNotificationOptions() {
        setUpOptionMenus();
        ReflectionHelpers.setField(mFragment, "mListType", LIST_TYPE_NOTIFICATION);
        ReflectionHelpers.setField(mFragment, "mOptionsMenu", mMenu);

        mFragment.updateOptionsMenu();
        assertThat(mMenu.findItem(R.id.sort_order_recent_notification).isVisible()).isFalse();
        assertThat(mMenu.findItem(R.id.sort_order_frequent_notification).isVisible()).isFalse();
    }


    @Test
    public void onCreateView_shouldNotShowLoadingContainer() {
        ReflectionHelpers.setField(mFragment, "mResetAppsHelper", mock(ResetAppsHelper.class));
        doNothing().when(mFragment).createHeader();

        final LayoutInflater layoutInflater = mock(LayoutInflater.class);
        final View view = mock(View.class);
        final View loadingContainer = mock(View.class);
        when(layoutInflater.inflate(anyInt(), eq(null))).thenReturn(view);
        when(view.findViewById(R.id.loading_container)).thenReturn(loadingContainer);

        mFragment.onCreateView(layoutInflater, mock(ViewGroup.class), null);

        verify(loadingContainer, never()).setVisibility(View.VISIBLE);
    }

    @Test
    public void onCreateOptionsMenu_shouldSetSearchQueryListener() {
        final SearchView searchView = mock(SearchView.class);
        final MenuItem searchMenu = mock(MenuItem.class);
        final MenuItem helpMenu = mock(MenuItem.class);
        when(searchMenu.getActionView()).thenReturn(searchView);
        when(mMenu.findItem(R.id.search_app_list_menu)).thenReturn(searchMenu);
        when(mMenu.add(anyInt() /* groupId */, anyInt() /* itemId */, anyInt() /* order */,
            anyInt() /* titleRes */)).thenReturn(helpMenu);
        doReturn("Test").when(mFragment).getText(anyInt() /* resId */);
        doNothing().when(mFragment).updateOptionsMenu();

        mFragment.onCreateOptionsMenu(mMenu, mock(MenuInflater.class));

        verify(searchView).setOnQueryTextListener(mFragment);
    }

    @Test
    public void onQueryTextChange_shouldFilterSearchInApplicationsAdapter() {
        final ManageApplications.ApplicationsAdapter adapter =
            mock(ManageApplications.ApplicationsAdapter.class);
        final String query = "Test App";
        ReflectionHelpers.setField(mFragment, "mApplications", adapter);

        mFragment.onQueryTextChange(query);

        verify(adapter).filterSearch(query);
    }

    @Test
    public void updateLoading_appLoaded_shouldNotDelayCallToHandleLoadingContainer() {
        ReflectionHelpers.setField(mFragment, "mLoadingContainer", mock(View.class));
        ReflectionHelpers.setField(mFragment, "mListContainer", mock(View.class));
        final ManageApplications.ApplicationsAdapter adapter =
                spy(new ManageApplications.ApplicationsAdapter(mState, mFragment,
                        AppFilterRegistry.getInstance().get(FILTER_APPS_ALL), new Bundle()));
        final LoadingViewController loadingViewController =
                mock(LoadingViewController.class);
        ReflectionHelpers.setField(adapter, "mLoadingViewController", loadingViewController);

        // app loading completed
        ReflectionHelpers.setField(adapter, "mHasReceivedLoadEntries", true);
        final ArrayList<ApplicationsState.AppEntry> appList = new ArrayList<>();
        appList.add(mock(ApplicationsState.AppEntry.class));
        when(mSession.getAllApps()).thenReturn(appList);

        adapter.updateLoading();

        verify(loadingViewController, never()).showLoadingViewDelayed();
    }

    @Test
    public void updateLoading_appNotLoaded_shouldDelayCallToHandleLoadingContainer() {
        ReflectionHelpers.setField(mFragment, "mLoadingContainer", mock(View.class));
        ReflectionHelpers.setField(mFragment, "mListContainer", mock(View.class));
        final ManageApplications.ApplicationsAdapter adapter =
                spy(new ManageApplications.ApplicationsAdapter(mState, mFragment,
                        AppFilterRegistry.getInstance().get(FILTER_APPS_ALL), new Bundle()));
        final LoadingViewController loadingViewController =
                mock(LoadingViewController.class);
        ReflectionHelpers.setField(adapter, "mLoadingViewController", loadingViewController);

        // app loading not yet completed
        ReflectionHelpers.setField(adapter, "mHasReceivedLoadEntries", false);

        adapter.updateLoading();

        verify(loadingViewController).showLoadingViewDelayed();
    }

    @Test
    public void shouldUseStableItemHeight() {
        assertThat(ManageApplications.ApplicationsAdapter.shouldUseStableItemHeight(
                LIST_TYPE_MAIN))
                .isTrue();
        assertThat(ManageApplications.ApplicationsAdapter.shouldUseStableItemHeight(
                LIST_TYPE_NOTIFICATION))
                .isTrue();
    }

    @Test
    public void onRebuildComplete_shouldHideLoadingView() {
        final Context context = RuntimeEnvironment.application;
        final RecyclerView recyclerView = mock(RecyclerView.class);
        final View emptyView = mock(View.class);
        ReflectionHelpers.setField(mFragment, "mRecyclerView", recyclerView);
        ReflectionHelpers.setField(mFragment, "mEmptyView", emptyView);
        final View loadingContainer = mock(View.class);
        when(loadingContainer.getContext()).thenReturn(context);
        final View listContainer = mock(View.class);
        when(listContainer.getVisibility()).thenReturn(View.INVISIBLE);
        when(listContainer.getContext()).thenReturn(context);
        ReflectionHelpers.setField(mFragment, "mLoadingContainer", loadingContainer);
        ReflectionHelpers.setField(mFragment, "mListContainer", listContainer);
        final ManageApplications.ApplicationsAdapter adapter =
                spy(new ManageApplications.ApplicationsAdapter(mState, mFragment,
                        AppFilterRegistry.getInstance().get(FILTER_APPS_ALL), new Bundle()));
        final LoadingViewController loadingViewController =
                mock(LoadingViewController.class);
        ReflectionHelpers.setField(adapter, "mLoadingViewController", loadingViewController);
        ReflectionHelpers.setField(adapter, "mAppFilter",
                AppFilterRegistry.getInstance().get(FILTER_APPS_ALL));

        // app loading not yet completed
        ReflectionHelpers.setField(adapter, "mHasReceivedLoadEntries", false);
        adapter.updateLoading();

        // app loading completed
        ReflectionHelpers.setField(adapter, "mHasReceivedLoadEntries", true);
        final ArrayList<ApplicationsState.AppEntry> appList = new ArrayList<>();
        appList.add(mock(ApplicationsState.AppEntry.class));
        when(mSession.getAllApps()).thenReturn(appList);
        ReflectionHelpers.setField(
                mFragment, "mFilterAdapter", mock(ManageApplications.FilterSpinnerAdapter.class));

        adapter.onRebuildComplete(null);

        verify(loadingViewController).showContent(true /* animate */);
    }

    @Test
    public void onRebuildComplete_hasSearchQuery_shouldFilterSearch() {
        final String query = "Test";
        final RecyclerView recyclerView = mock(RecyclerView.class);
        final View emptyView = mock(View.class);
        ReflectionHelpers.setField(mFragment, "mRecyclerView", recyclerView);
        ReflectionHelpers.setField(mFragment, "mEmptyView", emptyView);
        final SearchView searchView = mock(SearchView.class);
        ReflectionHelpers.setField(mFragment, "mSearchView", searchView);
        when(searchView.isVisibleToUser()).thenReturn(true);
        when(searchView.getQuery()).thenReturn(query);
        final View listContainer = mock(View.class);
        when(listContainer.getVisibility()).thenReturn(View.VISIBLE);
        ReflectionHelpers.setField(mFragment, "mListContainer", listContainer);
        ReflectionHelpers.setField(
            mFragment, "mFilterAdapter", mock(ManageApplications.FilterSpinnerAdapter.class));
        final ArrayList<ApplicationsState.AppEntry> appList = new ArrayList<>();
        appList.add(mock(ApplicationsState.AppEntry.class));
        final ManageApplications.ApplicationsAdapter adapter =
            spy(new ManageApplications.ApplicationsAdapter(mState, mFragment,
                AppFilterRegistry.getInstance().get(FILTER_APPS_ALL),
                null /* savedInstanceState */));

        adapter.onRebuildComplete(appList);

        verify(adapter).filterSearch(query);
    }

    @Test
    public void notifyItemChange_recyclerViewIdle_shouldNotify() {
        final RecyclerView recyclerView = mock(RecyclerView.class);
        final ManageApplications.ApplicationsAdapter adapter =
                spy(new ManageApplications.ApplicationsAdapter(mState, mFragment,
                        AppFilterRegistry.getInstance().get(FILTER_APPS_ALL), new Bundle()));

        adapter.onAttachedToRecyclerView(recyclerView);
        adapter.mOnScrollListener.onScrollStateChanged(recyclerView, SCROLL_STATE_IDLE);
        adapter.mOnScrollListener.postNotifyItemChange(0 /* index */);

        verify(adapter).notifyItemChanged(0);
    }

    @Test
    public void notifyItemChange_recyclerViewScrolling_shouldNotifyWhenIdle() {
        final RecyclerView recyclerView = mock(RecyclerView.class);
        final ManageApplications.ApplicationsAdapter adapter =
                spy(new ManageApplications.ApplicationsAdapter(mState, mFragment,
                        AppFilterRegistry.getInstance().get(FILTER_APPS_ALL), new Bundle()));

        adapter.onAttachedToRecyclerView(recyclerView);
        adapter.mOnScrollListener.onScrollStateChanged(recyclerView, SCROLL_STATE_DRAGGING);
        adapter.mOnScrollListener.postNotifyItemChange(0 /* index */);

        verify(adapter, never()).notifyItemChanged(0);
        verify(adapter, never()).notifyDataSetChanged();

        adapter.mOnScrollListener.onScrollStateChanged(recyclerView, SCROLL_STATE_IDLE);
        verify(adapter).notifyDataSetChanged();
    }

    @Test
    public void applicationsAdapter_onBindViewHolder_notifications_wrongExtraInfo() {
        when(mUserManager.getProfileIdsWithDisabled(anyInt())).thenReturn(new int[]{});
        ReflectionHelpers.setField(mFragment, "mUserManager", mUserManager);
        mFragment.mListType = LIST_TYPE_NOTIFICATION;
        ApplicationViewHolder holder = mock(ApplicationViewHolder.class);
        ReflectionHelpers.setField(holder, "itemView", mock(View.class));
        ManageApplications.ApplicationsAdapter adapter =
                new ManageApplications.ApplicationsAdapter(mState,
                        mFragment, mock(AppFilterItem.class),
                        mock(Bundle.class));
        final ArrayList<ApplicationsState.AppEntry> appList = new ArrayList<>();
        final ApplicationsState.AppEntry appEntry = mock(ApplicationsState.AppEntry.class);
        appEntry.info = mock(ApplicationInfo.class);
        appEntry.extraInfo = mock(AppFilterItem.class);
        appList.add(appEntry);
        ReflectionHelpers.setField(adapter, "mEntries", appList);

        adapter.onBindViewHolder(holder, 0);
        // no crash? yay!
    }

    @Test
    public void applicationsAdapter_onBindViewHolder_updateSwitch_notifications() {
        when(mUserManager.getProfileIdsWithDisabled(anyInt())).thenReturn(new int[]{});
        ReflectionHelpers.setField(mFragment, "mUserManager", mUserManager);
        mFragment.mListType = LIST_TYPE_NOTIFICATION;
        ApplicationViewHolder holder = mock(ApplicationViewHolder.class);
        ReflectionHelpers.setField(holder, "itemView", mock(View.class));
        ManageApplications.ApplicationsAdapter adapter =
                new ManageApplications.ApplicationsAdapter(mState,
                    mFragment, mock(AppFilterItem.class),
                        mock(Bundle.class));
        final ArrayList<ApplicationsState.AppEntry> appList = new ArrayList<>();
        final ApplicationsState.AppEntry appEntry = mock(ApplicationsState.AppEntry.class);
        appEntry.info = mock(ApplicationInfo.class);
        appList.add(appEntry);
        ReflectionHelpers.setField(adapter, "mEntries", appList);

        adapter.onBindViewHolder(holder, 0);
        verify(holder).updateSwitch(any(), anyBoolean(), anyBoolean());
    }

    @Test
    public void applicationsAdapter_onBindViewHolder_updateSwitch_notNotifications() {
        mFragment.mListType = LIST_TYPE_MAIN;
        ApplicationViewHolder holder = mock(ApplicationViewHolder.class);
        ReflectionHelpers.setField(holder, "itemView", mock(View.class));
        when(mUserManager.getProfileIdsWithDisabled(anyInt())).thenReturn(new int[]{});
        ReflectionHelpers.setField(mFragment, "mUserManager", mUserManager);
        ManageApplications.ApplicationsAdapter adapter = new ManageApplications.ApplicationsAdapter(
                mState, mFragment, mock(AppFilterItem.class), mock(Bundle.class));
        final ArrayList<ApplicationsState.AppEntry> appList = new ArrayList<>();
        final ApplicationsState.AppEntry appEntry = mock(ApplicationsState.AppEntry.class);
        appEntry.info = mock(ApplicationInfo.class);
        appList.add(appEntry);
        ReflectionHelpers.setField(adapter, "mEntries", appList);

        adapter.onBindViewHolder(holder, 0);
        verify(holder, never()).updateSwitch(any(), anyBoolean(), anyBoolean());
    }

    @Test
    public void applicationsAdapter_filterSearch_emptyQuery_shouldShowFullList() {
        final ManageApplications.ApplicationsAdapter adapter =
            new ManageApplications.ApplicationsAdapter(
                mState, mFragment, mock(AppFilterItem.class), Bundle.EMPTY);
        final String[] appNames = {"Apricot", "Banana", "Cantaloupe", "Fig", "Mango"};
        ReflectionHelpers.setField(adapter, "mOriginalEntries", getTestAppList(appNames));

        adapter.filterSearch("");

        assertThat(adapter.getItemCount()).isEqualTo(5);
    }

    @Test
    public void applicationsAdapter_filterSearch_noMatch_shouldShowEmptyList() {
        final ManageApplications.ApplicationsAdapter adapter =
            new ManageApplications.ApplicationsAdapter(
                mState, mFragment, mock(AppFilterItem.class), Bundle.EMPTY);
        final String[] appNames = {"Apricot", "Banana", "Cantaloupe", "Fig", "Mango"};
        ReflectionHelpers.setField(adapter, "mOriginalEntries", getTestAppList(appNames));

        adapter.filterSearch("orange");

        assertThat(adapter.getItemCount()).isEqualTo(0);
    }

    @Test
    public void applicationsAdapter_filterSearch_shouldShowMatchedItemsOnly() {
        final ManageApplications.ApplicationsAdapter adapter =
            new ManageApplications.ApplicationsAdapter(
                mState, mFragment, mock(AppFilterItem.class), Bundle.EMPTY);
        final String[] appNames = {"Apricot", "Banana", "Cantaloupe", "Fig", "Mango"};
        ReflectionHelpers.setField(adapter, "mOriginalEntries", getTestAppList(appNames));

        adapter.filterSearch("an");

        assertThat(adapter.getItemCount()).isEqualTo(3);
        assertThat(adapter.getAppEntry(0).label).isEqualTo("Banana");
        assertThat(adapter.getAppEntry(1).label).isEqualTo("Cantaloupe");
        assertThat(adapter.getAppEntry(2).label).isEqualTo("Mango");
    }

    @Test
    public void sortOrderSavedOnRebuild() {
        when(mUserManager.getProfileIdsWithDisabled(anyInt())).thenReturn(new int[]{});
        ReflectionHelpers.setField(mFragment, "mUserManager", mUserManager);
        mFragment.mListType = LIST_TYPE_NOTIFICATION;
        mFragment.mSortOrder = -1;
        ManageApplications.ApplicationsAdapter adapter = new ManageApplications.ApplicationsAdapter(
                mState, mFragment, mock(AppFilterItem.class), mock(Bundle.class));

        adapter.rebuild(mSortRecent.getItemId());
        assertThat(mFragment.mSortOrder).isEqualTo(mSortRecent.getItemId());

        adapter.rebuild(mSortFrequent.getItemId());
        assertThat(mFragment.mSortOrder).isEqualTo(mSortFrequent.getItemId());
    }

    private void setUpOptionMenus() {
        when(mMenu.findItem(anyInt())).thenAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            final int id = (int) args[0];
            if (id == mAppReset.getItemId()) {
                return mAppReset;
            }
            if (id == mSortFrequent.getItemId()) {
                return mSortFrequent;
            }
            if (id == mSortRecent.getItemId()) {
                return mSortRecent;
            }
            return new RoboMenuItem(id);
        });
    }

    private ArrayList<ApplicationsState.AppEntry> getTestAppList(String[] appNames) {
        final ArrayList<ApplicationsState.AppEntry> appList = new ArrayList<>();
        for (String name : appNames) {
            final ApplicationsState.AppEntry appEntry = mock(ApplicationsState.AppEntry.class);
            appEntry.label = name;
            appList.add(appEntry);
        }
        return appList;
    }
}
