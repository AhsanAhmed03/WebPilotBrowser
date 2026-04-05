package com.browser.app;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.browser.app.databinding.ActivityMainBindingImpl;
import com.browser.app.databinding.BottomSheetTabsBindingImpl;
import com.browser.app.databinding.DialogBookmarksHistoryBindingImpl;
import com.browser.app.databinding.DialogDownloadsBindingImpl;
import com.browser.app.databinding.LayoutHomePageBindingImpl;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_ACTIVITYMAIN = 1;

  private static final int LAYOUT_BOTTOMSHEETTABS = 2;

  private static final int LAYOUT_DIALOGBOOKMARKSHISTORY = 3;

  private static final int LAYOUT_DIALOGDOWNLOADS = 4;

  private static final int LAYOUT_LAYOUTHOMEPAGE = 5;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(5);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.browser.app.R.layout.activity_main, LAYOUT_ACTIVITYMAIN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.browser.app.R.layout.bottom_sheet_tabs, LAYOUT_BOTTOMSHEETTABS);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.browser.app.R.layout.dialog_bookmarks_history, LAYOUT_DIALOGBOOKMARKSHISTORY);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.browser.app.R.layout.dialog_downloads, LAYOUT_DIALOGDOWNLOADS);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.browser.app.R.layout.layout_home_page, LAYOUT_LAYOUTHOMEPAGE);
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
        case  LAYOUT_ACTIVITYMAIN: {
          if ("layout/activity_main_0".equals(tag)) {
            return new ActivityMainBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_main is invalid. Received: " + tag);
        }
        case  LAYOUT_BOTTOMSHEETTABS: {
          if ("layout/bottom_sheet_tabs_0".equals(tag)) {
            return new BottomSheetTabsBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for bottom_sheet_tabs is invalid. Received: " + tag);
        }
        case  LAYOUT_DIALOGBOOKMARKSHISTORY: {
          if ("layout/dialog_bookmarks_history_0".equals(tag)) {
            return new DialogBookmarksHistoryBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for dialog_bookmarks_history is invalid. Received: " + tag);
        }
        case  LAYOUT_DIALOGDOWNLOADS: {
          if ("layout/dialog_downloads_0".equals(tag)) {
            return new DialogDownloadsBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for dialog_downloads is invalid. Received: " + tag);
        }
        case  LAYOUT_LAYOUTHOMEPAGE: {
          if ("layout/layout_home_page_0".equals(tag)) {
            return new LayoutHomePageBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for layout_home_page is invalid. Received: " + tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(1);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(1);

    static {
      sKeys.put(0, "_all");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(5);

    static {
      sKeys.put("layout/activity_main_0", com.browser.app.R.layout.activity_main);
      sKeys.put("layout/bottom_sheet_tabs_0", com.browser.app.R.layout.bottom_sheet_tabs);
      sKeys.put("layout/dialog_bookmarks_history_0", com.browser.app.R.layout.dialog_bookmarks_history);
      sKeys.put("layout/dialog_downloads_0", com.browser.app.R.layout.dialog_downloads);
      sKeys.put("layout/layout_home_page_0", com.browser.app.R.layout.layout_home_page);
    }
  }
}
