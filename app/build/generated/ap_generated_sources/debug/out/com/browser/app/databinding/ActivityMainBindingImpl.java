package com.browser.app.databinding;
import com.browser.app.R;
import com.browser.app.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityMainBindingImpl extends ActivityMainBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = new androidx.databinding.ViewDataBinding.IncludedLayouts(22);
        sIncludes.setIncludes(1, 
            new String[] {"layout_home_page"},
            new int[] {2},
            new int[] {com.browser.app.R.layout.layout_home_page});
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.toolbarLayout, 3);
        sViewsWithIds.put(R.id.btnBack, 4);
        sViewsWithIds.put(R.id.btnForward, 5);
        sViewsWithIds.put(R.id.secureIcon, 6);
        sViewsWithIds.put(R.id.urlBar, 7);
        sViewsWithIds.put(R.id.btnClearUrl, 8);
        sViewsWithIds.put(R.id.btnReload, 9);
        sViewsWithIds.put(R.id.progressBar, 10);
        sViewsWithIds.put(R.id.suggestionCard, 11);
        sViewsWithIds.put(R.id.rvSuggestions, 12);
        sViewsWithIds.put(R.id.swipeRefresh, 13);
        sViewsWithIds.put(R.id.bottomNavBar, 14);
        sViewsWithIds.put(R.id.btnBookmarks, 15);
        sViewsWithIds.put(R.id.btnHistory, 16);
        sViewsWithIds.put(R.id.btnHome, 17);
        sViewsWithIds.put(R.id.btnTabsContainer, 18);
        sViewsWithIds.put(R.id.btnTabs, 19);
        sViewsWithIds.put(R.id.tabCountBadge, 20);
        sViewsWithIds.put(R.id.btnSettings, 21);
    }
    // views
    @NonNull
    private final androidx.constraintlayout.widget.ConstraintLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityMainBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 22, sIncludes, sViewsWithIds));
    }
    private ActivityMainBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 1
            , (android.widget.LinearLayout) bindings[14]
            , (android.widget.ImageButton) bindings[4]
            , (android.widget.LinearLayout) bindings[15]
            , (android.widget.ImageButton) bindings[8]
            , (android.widget.ImageButton) bindings[5]
            , (android.widget.LinearLayout) bindings[16]
            , (android.widget.LinearLayout) bindings[17]
            , (android.widget.ImageButton) bindings[9]
            , (android.widget.LinearLayout) bindings[21]
            , (android.widget.ImageView) bindings[19]
            , (android.widget.LinearLayout) bindings[18]
            , (com.browser.app.databinding.LayoutHomePageBinding) bindings[2]
            , (android.widget.ProgressBar) bindings[10]
            , (androidx.recyclerview.widget.RecyclerView) bindings[12]
            , (android.widget.ImageView) bindings[6]
            , (androidx.cardview.widget.CardView) bindings[11]
            , (com.browser.app.BrowserSwipeRefreshLayout) bindings[13]
            , (android.widget.TextView) bindings[20]
            , (android.widget.LinearLayout) bindings[3]
            , (android.widget.EditText) bindings[7]
            , (android.widget.FrameLayout) bindings[1]
            );
        setContainedBinding(this.homePage);
        this.mboundView0 = (androidx.constraintlayout.widget.ConstraintLayout) bindings[0];
        this.mboundView0.setTag(null);
        this.webViewContainer.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x2L;
        }
        homePage.invalidateAll();
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        if (homePage.hasPendingBindings()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
            return variableSet;
    }

    @Override
    public void setLifecycleOwner(@Nullable androidx.lifecycle.LifecycleOwner lifecycleOwner) {
        super.setLifecycleOwner(lifecycleOwner);
        homePage.setLifecycleOwner(lifecycleOwner);
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
            case 0 :
                return onChangeHomePage((com.browser.app.databinding.LayoutHomePageBinding) object, fieldId);
        }
        return false;
    }
    private boolean onChangeHomePage(com.browser.app.databinding.LayoutHomePageBinding HomePage, int fieldId) {
        if (fieldId == BR._all) {
            synchronized(this) {
                    mDirtyFlags |= 0x1L;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        // batch finished
        executeBindingsOn(homePage);
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): homePage
        flag 1 (0x2L): null
    flag mapping end*/
    //end
}