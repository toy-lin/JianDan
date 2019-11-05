package com.socks.jiandan.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by zhaokaiqiang on 15/4/24.
 */
public class FadeInAnimator extends RecyclerView.ItemAnimator{

	private ArrayList<RecyclerView.ViewHolder> viewHolders;

	public FadeInAnimator(){
		viewHolders = new ArrayList<>();
	}

	@Override
	public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
		return false;
	}

	@Override
	public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
		return false;
	}

	@Override
	public boolean animatePersistence(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
		return false;
	}

	@Override
	public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
		return false;
	}


	@Override
	public void runPendingAnimations() {

	}


	@Override
	public void endAnimation(RecyclerView.ViewHolder item) {

	}

	@Override
	public void endAnimations() {

	}

	@Override
	public boolean isRunning() {
		return false;
	}
}
