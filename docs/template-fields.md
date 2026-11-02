# Template field reference

Use this reference when comparing a template in the WeChat dashboard with the
data assembled by the application. Placeholder names are case-sensitive and use
the form `{{fieldName.DATA}}`.

## Choose a shipped template

| Profile | Chinese template | English template | Required date arguments |
| --- | --- | --- | --- |
| Weather and poetry | [basic.txt](../templates/basic.txt) | [basic.en.txt](../templates/basic.en.txt) | None |
| Birthday | [birthday.txt](../templates/birthday.txt) | [birthday.en.txt](../templates/birthday.en.txt) | One birthday |
| Couple | [couple.txt](../templates/couple.txt) | [couple.en.txt](../templates/couple.en.txt) | Two birthdays, elapsed-day anniversary, anniversary countdown |

Choose the language with `MainConfig.messageLanguage` (`zh` or `en`). Copy the
matching template body into WeChat and use its template ID in `WechatConfig.java`.
Editing a local template file does not update the dashboard template.

## Fields shared by all six templates

| Placeholder | Value and conditions |
| --- | --- |
| `{{userName.DATA}}` | Configured nickname; a blank nickname falls back to the language's default greeting. |
| `{{date.DATA}}` | Today's date in `YYYY-MM-DD` format. The application entry point sets the timezone to GMT+8. |
| `{{week.DATA}}` | Today's weekday; English mode uses full names such as `Monday`. |
| `{{randomInfo.DATA}}` | Random poetry in the default `randomApiMessageMode = true` mode. Chinese mode uses the poetry API with a local fallback; English mode uses bundled micro-poems. |
| `{{weatherTodayDay.DATA}}` | Today's daytime forecast. |
| `{{weatherTodayNight.DATA}}` | Today's nighttime forecast. |
| `{{weatherTomorrowDay.DATA}}` | Tomorrow's daytime forecast. |
| `{{weatherTomorrowNight.DATA}}` | Tomorrow's nighttime forecast. |

The four weather fields require weather to be enabled and forecast mode, as in
the shipped configuration. They contain conditions, temperature, wind direction,
and wind force. An unavailable forecast keeps a localized fallback message;
these fields are not live temperature readings. Keep poetry before weather, as
in the supplied templates.

## Date fields

The argument position is counted **after the city argument** in `getUser(...)`.
The names do not choose how the value is calculated: each `BirthDay` object's
`countFlag` determines elapsed days (`true`) or a countdown (`false`).

| Position | Placeholder | Intended argument for the shipped templates |
| --- | --- | --- |
| 1 | `{{birthdayLeft.DATA}}` | Your birthday, with `countFlag = false`. |
| 2 | `{{partnerBirthdayLeft.DATA}}` | Partner's birthday, with `countFlag = false`. |
| 3 | `{{loveDays.DATA}}` | Anniversary start date, with `countFlag = true`. |
| 4 | `{{anniversaryLeft.DATA}}` | The same anniversary, with `countFlag = false`. |

Missing date arguments leave the corresponding date fields absent. A basic
profile therefore cannot fill a birthday or couple template. Elapsed days do
not add one; countdowns are zero on the event date. See the README for lunar
dates, leap-day birthdays, and complete configuration examples.

## Diagnosing missing fields

1. Compare the dashboard body with the matching local template, including the
   exact capitalization and `.DATA` suffix.
2. Check date argument positions and the selected language. Check that the
   template ID belongs to the current WeChat account and recipient profile.
3. Leave `randomApiMessageMode = true` for the supplied `randomInfo` placeholder.
   In non-random mode the application populates individual API keys such as
   `poem` instead; the supplied templates would need corresponding changes.
4. Run configuration validation before sending. It checks configuration values
   and dates, but does **not** fetch or compare the template stored in WeChat.
5. After an intentional send, verify the phone display. API acceptance alone
   does not prove that every field was displayed.

This table covers the shipped templates, not every legacy key in
[KeyConfig.java](../src/main/java/com/pt/vx/config/KeyConfig.java). Mapping logic is
in [MessageService.java](../src/main/java/com/pt/vx/service/vx/MessageService.java);
[PortableMessageTest.java](../src/test/java/com/pt/vx/service/vx/PortableMessageTest.java)
checks that each supplied template has its required payload fields.
