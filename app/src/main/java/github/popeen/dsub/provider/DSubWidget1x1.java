/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2010 (C) Sindre Mehus
 */
package github.popeen.dsub.provider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import github.popeen.dsub.R;
import github.popeen.dsub.activity.SubsonicActivity;
import github.popeen.dsub.domain.MusicDirectory;
import github.popeen.dsub.util.ImageLoader;

public class DSubWidget1x1 extends DSubWidgetProvider {
	@Override
	protected int getLayout() {
		return R.layout.appwidget1x1;
	}

	@Override
	protected void setText(RemoteViews views, String title, CharSequence artist, CharSequence album, CharSequence errorState) {
		if (errorState != null) {
			views.setTextViewText(R.id.album, errorState);
		}
		else {
			views.setTextViewText(R.id.album, album);
		}
	}

	@Override
	protected void setImage(RemoteViews views, Context context, MusicDirectory.Entry currentPlaying)
	{
		// Set the cover art
		try {
			ImageLoader imageLoader = SubsonicActivity.getStaticImageLoader(context);
			Bitmap bitmap = imageLoader == null ? null : imageLoader.getCachedImage(context, currentPlaying, false);

			if (bitmap == null) {
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.appwidget_art_unknown);
			}

			bitmap = getRoundedCornerBitmap(bitmap, 50);
			views.setImageViewBitmap(R.id.appwidget_coverart, bitmap);
		} catch (Exception x) {
			Log.e(TAG, "Failed to load cover art", x);
			views.setImageViewResource(R.id.appwidget_coverart, R.drawable.appwidget_art_unknown);
		}
	}
}
