package net.osmand.plus.profiles;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import java.util.List;
import net.osmand.PlatformUtil;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.base.BottomSheetDialogFragment;
import org.apache.commons.logging.Log;

public class NavTypeBottomSheetDialogFragment extends BottomSheetDialogFragment {

	private static final Log LOG = PlatformUtil.getLog(NavTypeBottomSheetDialogFragment.class);

	private List<RoutingProfile> routingProfiles;
	private NavTypeDialogListener listener;
	private NavTypeDialogListener listListener;
	private RecyclerView recyclerView;
	private NavTypeAdapter adapter;

	public void setNavTypeListener(NavTypeDialogListener listener) {
		this.listener = listener;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {

		Bundle args = getArguments();
		if (args != null) {
			routingProfiles = args.getParcelableArrayList("routing_profiles");
		}

		final int themeRes = getMyApplication().getSettings().isLightContent()
			? R.style.OsmandLightTheme
			: R.style.OsmandDarkTheme;
		View view = View.inflate(new ContextThemeWrapper(getContext(), themeRes),
			R.layout.bottom_sheet_select_type_fragment, null);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		listListener = new NavTypeDialogListener() {
			@Override
			public void selectedNavType(int pos) {
				listener.selectedNavType(pos);
				dismiss();
			}
		};
		recyclerView = view.findViewById(R.id.menu_list_view);
		adapter = new NavTypeAdapter(routingProfiles, isNightMode(getMyApplication()),
			listListener);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(adapter);
		Button cancelBtn = view.findViewById(R.id.cancel_selection);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return view;
	}

	private static boolean isNightMode(OsmandApplication ctx) {
		return !ctx.getSettings().isLightContent();
	}

	class NavTypeAdapter extends RecyclerView.Adapter<ItemViewHolder> {

		private final List<RoutingProfile> items;
		private final boolean isNightMode;
		private NavTypeDialogListener listener;
		private int previousSelection;

		public NavTypeAdapter(@NonNull List<RoutingProfile> objects,
			@NonNull boolean isNightMode, NavTypeDialogListener listener) {
			this.items = objects;
			this.isNightMode = isNightMode;
			this.listener = listener;
		}

		public void updateData(List<RoutingProfile> newItems) {
			items.clear();
			items.addAll(newItems);
			notifyDataSetChanged();
		}

		@NonNull
		@Override
		public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new ItemViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.bottom_sheet_item_with_descr_and_radio_btn, parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
			final int pos = position;
			final RoutingProfile item = items.get(position);
			holder.title.setText(item.getName());
			holder.descr.setText(item.getOrigin());
			holder.icon.setImageDrawable(getIcon(item.getIconRes(), isNightMode
				? R.color.active_buttons_and_links_dark
				: R.color.active_buttons_and_links_light));
			if (item.isSelected()) {
				holder.radioButton.setChecked(true);
				previousSelection = position;
			} else {
				holder.radioButton.setChecked(false);
			}
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.selectedNavType(pos);
					holder.radioButton.setChecked(true);
					items.get(pos).setSelected(true);
					items.get(previousSelection).setSelected(false);
					notifyItemChanged(previousSelection);
					previousSelection = pos;
				}
			});
		}

		@Override
		public int getItemCount() {
			return items.size();
		}
	}

	class ItemViewHolder extends RecyclerView.ViewHolder {
		TextView title, descr;
		RadioButton radioButton;
		ImageView icon;

		public ItemViewHolder(View itemView) {
			super(itemView);
			title = itemView.findViewById(R.id.title);
			descr = itemView.findViewById(R.id.subtitle);
			radioButton = itemView.findViewById(R.id.compound_button);
			icon = itemView.findViewById(R.id.icon);
		}
	}

	interface NavTypeDialogListener {

		void selectedNavType(int pos);
	}


}