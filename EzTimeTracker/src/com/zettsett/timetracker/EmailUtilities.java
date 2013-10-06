package com.zettsett.timetracker;

import android.content.Context;
import android.content.Intent;

public class EmailUtilities {
	public static void send(final String address, final String subject,
			final Context context, final String textBody) {
		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { address });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, textBody);
		context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}

}
