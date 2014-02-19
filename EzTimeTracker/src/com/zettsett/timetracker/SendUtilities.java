package com.zettsett.timetracker;

import android.content.Context;
import android.content.Intent;

public class SendUtilities {
	public static void send(final String address, final String subject,
			final Context context, final String textBody) {
		final Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("text/*");
		intent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { address });
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(android.content.Intent.EXTRA_TEXT, textBody);
		context.startActivity(Intent.createChooser(intent, "Send to ..."));
	}

}
