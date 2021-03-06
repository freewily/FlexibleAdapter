package eu.davidea.samples.flexibleadapter.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * You should extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class SimpleItem extends AbstractModelItem<SimpleItem.ParentViewHolder>
		implements ISectionable<SimpleItem.ParentViewHolder, HeaderItem>, IFilterable, Serializable {

	private static final long serialVersionUID = -6882745111884490060L;

	/**
	 * The header of this item
	 */
	HeaderItem header;

	public SimpleItem(String id) {
		super(id);
		setDraggable(true);
		setSwipeable(true);
	}

	public SimpleItem(String id, HeaderItem header) {
		this(id);
		this.header = header;
	}

	@Override
	public HeaderItem getHeader() {
		return header;
	}

	@Override
	public void setHeader(HeaderItem header) {
		this.header = header;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_expandable_item;
	}

	@Override
	public ParentViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ParentViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	@SuppressWarnings({"unchecked", "deprecation"})
	public void bindViewHolder(final FlexibleAdapter adapter, ParentViewHolder holder, int position, List payloads) {
		//Subtitle
		if (adapter.isExpandable(this)) {
			setSubtitle(adapter.getCurrentChildren((IExpandable) this).size() + " subItems");
		} else {
			setSubtitle("Subtitle " + getId());
		}
		setSubtitle(getSubtitle() + (getHeader() != null ? " - Header: " + getHeader().getId() : ""));

		Context context = holder.itemView.getContext();
		int defColorAccent = context.getResources().getColor(R.color.colorAccent_light);

		if (adapter.isExpandable(this) && payloads.size() > 0){
			Log.i(this.getClass().getSimpleName(), "ExpandableItem Payload " + payloads);
			if (adapter.hasSearchText()) {
				Utils.highlightText(holder.itemView.getContext(), holder.mSubtitle,
						getSubtitle(), adapter.getSearchText(), defColorAccent);
			} else {
				holder.mSubtitle.setText(getSubtitle());
			}
			//We stop the process here, we only want to update the subtitle

		} else {
			//ANIMATION EXAMPLE!! ImageView - Handle Flip Animation on Select ALL and Deselect ALL
			if (adapter.isSelectAll() || adapter.isLastItemInActionMode()) {
				//Reset the flags with delay
				holder.itemView.postDelayed(new Runnable() {
					@Override
					public void run() {
						adapter.resetActionModeFlags();
					}
				}, 200L);
				//Consume the Animation
				holder.mFlipView.flip(adapter.isSelected(position), 200L);
			} else {
				//Display the current flip status
				holder.mFlipView.flipSilently(adapter.isSelected(position));
			}

			//In case of searchText matches with Title or with an SimpleItem's field
			// this will be highlighted
			if (adapter.hasSearchText()) {
				Utils.highlightText(context, holder.mTitle,
						getTitle(), adapter.getSearchText(), defColorAccent);
				Utils.highlightText(context, holder.mSubtitle,
						getSubtitle(), adapter.getSearchText(), defColorAccent);
			} else {
				holder.mTitle.setText(getTitle());
				holder.mSubtitle.setText(getSubtitle());
			}

			//This "if-else" is just an example of what you can do with item animation
			if (adapter.isSelected(position)) {
				adapter.animateView(holder.itemView, position, true);
			} else {
				adapter.animateView(holder.itemView, position, false);
			}
		}
	}

	@Override
	public boolean filter(String constraint) {
		return getTitle() != null && getTitle().toLowerCase().trim().contains(constraint) ||
				getSubtitle() != null && getSubtitle().toLowerCase().trim().contains(constraint);
	}

	/**
	 * This ViewHolder is expandable and collapsible.
	 */
	static final class ParentViewHolder extends ExpandableViewHolder {

		public FlipView mFlipView;
		public TextView mTitle;
		public TextView mSubtitle;
		public ImageView mHandleView;
		public Context mContext;
		private View frontView;
		private View rearLeftView;
		private View rearRightView;

		public ParentViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			this.mContext = view.getContext();
			this.mTitle = (TextView) view.findViewById(R.id.title);
			this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			this.mFlipView = (FlipView) view.findViewById(R.id.image);
			this.mFlipView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAdapter.mItemLongClickListener.onItemLongClick(getAdapterPosition());
					Toast.makeText(mContext, "ImageClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
					toggleActivation();
				}
			});
			this.mHandleView = (ImageView) view.findViewById(R.id.row_handle);
			setDragHandleView(mHandleView);

			this.frontView = view.findViewById(R.id.front_view);
			this.rearLeftView = view.findViewById(R.id.rear_left_view);
			this.rearRightView = view.findViewById(R.id.rear_right_view);
		}

		@Override
		protected void setDragHandleView(@NonNull View view) {
			if (mAdapter.isHandleDragEnabled()) {
				view.setVisibility(View.VISIBLE);
				super.setDragHandleView(view);
			} else {
				view.setVisibility(View.GONE);
			}
		}

		@Override
		public void onClick(View view) {
			Toast.makeText(mContext, "Click on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
			super.onClick(view);
		}

		@Override
		public boolean onLongClick(View view) {
			Toast.makeText(mContext, "LongClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
			return super.onLongClick(view);
		}

		@Override
		protected void toggleActivation() {
			super.toggleActivation();
			//Here we use a custom Animation inside the ItemView
			mFlipView.flip(mAdapter.isSelected(getAdapterPosition()));
		}

		@Override
		public float getActivationElevation() {
			return eu.davidea.utils.Utils.dpToPx(itemView.getContext(), 4f);
		}

		@Override
		protected boolean shouldActivateViewWhileSwiping() {
			return false;//default=false
		}

		@Override
		protected boolean shouldAddSelectionInActionMode() {
			return false;//default=false
		}

		@Override
		public View getFrontView() {
			return frontView;
		}

		@Override
		public View getRearLeftView() {
			return rearLeftView;
		}

		@Override
		public View getRearRightView() {
			return rearRightView;
		}
	}

	@Override
	public String toString() {
		return this instanceof ExpandableItem ? super.toString() :
				"SimpleItem[" + super.toString() + "]";
	}

}