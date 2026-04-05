package com.browser.app.databinding;
import com.browser.app.R;
import com.browser.app.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class LayoutHomePageBindingImpl extends LayoutHomePageBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.homeLogo, 1);
        sViewsWithIds.put(R.id.homeSearchBar, 2);
        sViewsWithIds.put(R.id.homeSearchHint, 3);
        sViewsWithIds.put(R.id.shortcutGoogle, 4);
        sViewsWithIds.put(R.id.shortcutYouTube, 5);
        sViewsWithIds.put(R.id.shortcutGitHub, 6);
        sViewsWithIds.put(R.id.shortcutReddit, 7);
        sViewsWithIds.put(R.id.shortcutTwitter, 8);
        sViewsWithIds.put(R.id.tvTrendingRegion, 9);
        sViewsWithIds.put(R.id.tvTrendingLoading, 10);
        sViewsWithIds.put(R.id.trendingContainer, 11);
        sViewsWithIds.put(R.id.rvTrending, 12);
        sViewsWithIds.put(R.id.rvTrendingExtra, 13);
        sViewsWithIds.put(R.id.tvTrendingToggle, 14);
        sViewsWithIds.put(R.id.tvNewsLoading, 15);
        sViewsWithIds.put(R.id.newsContainer, 16);
        sViewsWithIds.put(R.id.rvNews, 17);
        sViewsWithIds.put(R.id.rvNewsExtra, 18);
        sViewsWithIds.put(R.id.tvNewsToggle, 19);
    }
    // views
    @NonNull
    private final android.widget.ScrollView mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public LayoutHomePageBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 20, sIncludes, sViewsWithIds));
    }
    private LayoutHomePageBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.ImageView) bindings[1]
            , (android.widget.LinearLayout) bindings[2]
            , (android.widget.TextView) bindings[3]
            , (android.widget.LinearLayout) bindings[16]
            , (androidx.recyclerview.widget.RecyclerView) bindings[17]
            , (androidx.recyclerview.widget.RecyclerView) bindings[18]
            , (androidx.recyclerview.widget.RecyclerView) bindings[12]
            , (androidx.recyclerview.widget.RecyclerView) bindings[13]
            , (android.widget.LinearLayout) bindings[6]
            , (android.widget.LinearLayout) bindings[4]
            , (android.widget.LinearLayout) bindings[7]
            , (android.widget.LinearLayout) bindings[8]
            , (android.widget.LinearLayout) bindings[5]
            , (android.widget.LinearLayout) bindings[11]
            , (android.widget.TextView) bindings[15]
            , (android.widget.TextView) bindings[19]
            , (android.widget.TextView) bindings[10]
            , (android.widget.TextView) bindings[9]
            , (android.widget.TextView) bindings[14]
            );
        this.mboundView0 = (android.widget.ScrollView) bindings[0];
        this.mboundView0.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x1L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
            return variableSet;
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
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
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): null
    flag mapping end*/
    //end
}