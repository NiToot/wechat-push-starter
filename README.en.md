# WeChat Weather & Poetry Push · Deployment Starter

[中文](README.md) | **English**

A small Java application you can run in your own GitHub repository to send local weather forecasts, random Chinese poetry, and optional birthday and anniversary reminders through WeChat.

**Original account details, recipients, and personal dates have been removed. Scheduled sending is disabled by default. Manual workflow runs check configuration without sending unless you explicitly enable sending.**

[Start deployment](#step-1-prepare-your-accounts) · [Configure a recipient](#step-5-configure-one-recipient) · [Troubleshooting](#troubleshooting)

## Preview

These illustrations use fictional information. They are not phone screenshots and do not guarantee that WeChat will preserve colors, introductory text, or closing text. Message templates and preview images are in Chinese; this guide explains deployment in English.

### Weather and poetry

![Weather and poetry illustration](docs/images/preview-basic.png)

Use the [basic template](templates/basic.txt). No birthday configuration is required.

### Birthday reminder

![Birthday reminder illustration](docs/images/preview-birthday.png)

Use the [birthday template](templates/birthday.txt) and configure one birthday.

### Couple's anniversary

![Couple's anniversary illustration](docs/images/preview-couple.png)

Use the [couple template](templates/couple.txt). Configure your birthday, your partner's birthday, an elapsed-day anniversary, and an annual anniversary countdown in that order. The numbers in the image only illustrate the layout.

## What to configure

Two places require your own settings:

- **Your repository's GitHub Secrets**: `WECHAT_APP_ID`, `WECHAT_APP_SECRET`, and `AMAP_KEY`.
- **[WechatConfig.java](src/main/java/com/pt/vx/config/WechatConfig.java)**: each recipient's OpenID, template ID, nickname, detailed region, city, and optional dates.

You do not need to change the sending service, weather parser, or poetry API. Use your own credentials, OpenIDs, and template IDs.

## Step 1: Prepare your accounts

1. Sign in to the [WeChat Official Accounts sandbox](https://mp.weixin.qq.com/debug/cgi-bin/sandbox?t=sandbox/login) and obtain your AppID and AppSecret.
2. Each recipient must scan **your sandbox account's QR code** with their WeChat account and follow it. The identifier shown in the dashboard is their OpenID, not their nickname or WeChat ID.
3. Create an application and key in the [Amap console](https://console.amap.com/dev/key/app). Select **Web Service (Web服务)** as the service type. Verify access to geocoding and weather queries, with available quota. A WeChat Mini Program key is not suitable for this application.

## Step 2: Upload the complete source to your GitHub repository

1. Download and extract the source package.
2. Create your own repository. **Private** is recommended to protect recipient information and personal dates.
3. Upload the extracted files and directories. Keep `pom.xml`, `src/`, `templates/`, and `.github/` at the repository root. Do not upload only a ZIP file or wrap the project in an extra directory.
4. Confirm that `.github/workflows/maven.yml` is present.
5. If GitHub asks you to enable Actions, follow the instructions on the repository's Actions page.

With Git installed, you can also run the following from the extracted directory. Replace the URL with your own empty repository:

```bash
git init -b main
git add .
git commit -m "Initialize WeChat push starter"
git remote add origin https://github.com/YOUR_NAME/YOUR_REPO.git
git push -u origin main
```

## Step 3: Add three Secrets

In **your repository**, open:

`Settings → Secrets and variables → Actions → New repository secret`

Create these secrets:

- `WECHAT_APP_ID`: your WeChat sandbox AppID.
- `WECHAT_APP_SECRET`: your WeChat sandbox AppSecret.
- `AMAP_KEY`: your Amap Web Service key.

Names must match exactly. Do not put the values in Java files, the README, or the workflow source.

The workflow injects these environment variables only during configuration validation and sending. `.env.example` lists the variable names; Java **does not automatically load** a `.env` file.

Reference: [GitHub documentation on Secrets](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets).

## Step 4: Create a template in WeChat

For your first test, configure only yourself and use the basic template:

1. Open [templates/basic.txt](templates/basic.txt) and copy the entire contents.
2. In the WeChat sandbox dashboard, select “新增测试模板” (add a test template).
3. Enter a title such as “每日天气与诗句” (daily weather and poetry), paste the body, and save.
4. Copy the generated template ID into the recipient configuration in the next step.

Keep placeholders such as `{{userName.DATA}}` unchanged, including capitalization. Nickname, date, and weekday use separate labeled lines in the body, and poetry appears before weather. Do not restore unlabeled remarks.

Local template files are copies of the template bodies. Editing them **does not update existing templates in WeChat**. When switching sandbox accounts, create new templates; template IDs cannot be reused across accounts.

## Step 5: Configure one recipient

Open [WechatConfig.java](src/main/java/com/pt/vx/config/WechatConfig.java). The empty configuration is:

```java
userList.add(getUser(
    "", // 1. Your OpenID
    "", // 2. Your template ID
    "", // 3. Nickname
    "", // 4. Detailed region, preferably down to district/county
    ""  // 5. City
));
```

For example, a detailed region could be “浙江省杭州市西湖区” and the city “杭州”. These only illustrate the address format; replace them with your actual region.

Use standard double quotes and commas. Do not add a trailing comma after the city when it is the last argument. Omit `BirthDay` arguments for the basic weather and poetry template.

To add recipients, copy the entire `userList.add(getUser(...));` block and fill in each person's details. Every real send iterates over all configured recipients. Do not rerun a multi-recipient workflow just to test one person.

## Optional: Add birthdays or anniversaries

### Your birthday only

Add a comma after the city argument, then add a date. The following date is **fictional**:

```java
userList.add(getUser(
    "YOUR_OPENID",
    "YOUR_BIRTHDAY_TEMPLATE_ID",
    "Example user",
    "YOUR_DETAILED_REGION",
    "YOUR_CITY",
    new BirthDay(1995, 6, 19, false, false)
));
```

Use [birthday.txt](templates/birthday.txt) in WeChat. `false, false` means a Gregorian birthday countdown. For a Chinese lunar birthday, change the first boolean to `true`.

### Two birthdays and an anniversary

Use [couple.txt](templates/couple.txt) and keep the dates in this order. All dates below are fictional; **replace every date with your own**:

```java
userList.add(getUser(
    "YOUR_OPENID",
    "YOUR_COUPLE_TEMPLATE_ID",
    "Example user",
    "YOUR_DETAILED_REGION",
    "YOUR_CITY",
    new BirthDay(1995, 6, 19, false, false), // Your Gregorian birthday
    new BirthDay(1996, 1, 10, true, false),  // Partner's lunar birthday
    new BirthDay(2020, 1, 1, false, true),   // Gregorian anniversary: elapsed days
    new BirthDay(2020, 1, 1, false, false)   // Same anniversary: next annual occurrence
));
```

- The first date maps to `birthdayLeft`; the second maps to `partnerBirthdayLeft`.
- The third maps to `loveDays`; the fourth maps to `anniversaryLeft`.
- Arguments are: year, month, day, whether the date is lunar, and whether to count elapsed days.
- Elapsed days use the difference between dates without adding one. A countdown is zero on the event date.
- If the template's fixed text states a specific Gregorian or lunar birthday, update that text to match the configured date.
- February 29 counts down to the next actual February 29, without substituting February 28 or March 1. The configured birth year must also be a valid leap year.
- Lunar dates use regular months, with supported years from 1900 through 2099. If day 30 does not exist in a given lunar month, the application finds the next year containing that date instead of substituting day 29.
- This starter does not provide a leap-month birthday policy. Verify leap-month birthdays separately before enabling scheduled sends; do not substitute an ordinary lunar month without checking.

## Step 6: Validate, then send once

1. Commit your configuration changes to your repository's default branch.
2. Open `Actions → WeChat weather and poem → Run workflow`.
3. For the first run, **leave the send option unchecked**. The workflow builds, runs offline tests, and validates configuration.
4. Confirm that all steps pass and the logs contain no actual sending records.
5. Run the workflow again and explicitly check “实际向已配置收件人发送消息” (send messages to configured recipients). Only this run calls the sending API.
6. Check each recipient's phone for the nickname, date, weekday, poem, and all four weather forecasts.

Configuration validation checks required fields and duplicate recipients without contacting WeChat, Amap, or the poetry service. **It does not prove that API permissions, template IDs, or subscription status are valid.**

Weather values are daytime and nighttime forecasts for today and tomorrow, not current temperatures. If the poetry API fails, returns incomplete data, or returns text that is too long, a short local poem is used. Random poems may repeat.

## Step 7: Optionally enable daily sending

After a successful manual send, edit [.github/workflows/maven.yml](.github/workflows/maven.yml). Change:

```yaml
  # schedule:
  #   - cron: '8 0 * * *'
```

to:

```yaml
  schedule:
    - cron: '8 0 * * *'
```

Keep the indentation: `schedule` and `workflow_dispatch` are siblings under `on`. This expression means 00:08 UTC daily, or 08:08 Beijing time (UTC+8). Actual start times may be delayed; this is not a minute-precise alarm. Scheduled runs use the default branch and send to every configured recipient.

## Run locally (optional)

Use JDK 11 and Maven. The source targets Java 8; test compatibility with newer JDKs is not guaranteed by this package.

Windows PowerShell:

```powershell
$env:WECHAT_APP_ID = "YOUR_APP_ID"
$env:WECHAT_APP_SECRET = "YOUR_APP_SECRET"
$env:AMAP_KEY = "YOUR_AMAP_KEY"
mvn -B '-Dfile.encoding=UTF-8' package
java -jar target/VxMessageActions-1.0-SNAPSHOT.jar --check-config
# Execute only when you intend to send real messages:
java -jar target/VxMessageActions-1.0-SNAPSHOT.jar
```

Linux / macOS:

```bash
export WECHAT_APP_ID='YOUR_APP_ID'
export WECHAT_APP_SECRET='YOUR_APP_SECRET'
export AMAP_KEY='YOUR_AMAP_KEY'
mvn -B -Dfile.encoding=UTF-8 package
java -jar target/VxMessageActions-1.0-SNAPSHOT.jar --check-config
# This command sends real messages:
java -jar target/VxMessageActions-1.0-SNAPSHOT.jar
```

The first build needs internet access to download Maven dependencies. `mvn package` and `--check-config` do not send messages. Running the JAR without arguments sends real messages. Do not share screenshots or terminal history containing actual credentials.

## Troubleshooting

- **“请填写配置：WECHAT_APP_ID” or another missing-setting error**: check Secret names, values, and environment injection. Locally, check the current terminal's environment variables.
- **Error 43004**: the configured OpenID typically has not followed this sandbox account. Verify subscription status for the same account before rerunning a multi-recipient job.
- **Error 40037**: verify that the complete template ID belongs to the current sandbox account.
- **Amap 10009 / USERKEY_PLAT_NOMATCH**: check that the key is a Web Service key with the required service permissions.
- **Missing nickname, date, or poem**: compare the WeChat template body with the corresponding local template. Keep the fixed Chinese labels and place poetry before weather. API acceptance does not guarantee complete phone display.
- **Colors do not change**: random color parameters are retained, but WeChat clients may use default colors. Custom colors are not guaranteed.
- **An Actions run partially fails**: inspect each recipient's result. Successful recipients may already have received messages. Do not rerun the entire job to retry failures, especially when delivery status is uncertain.
- **Scheduled messages arrive late**: check the default branch, whether Actions is enabled, and workflow logs. GitHub scheduling can be delayed.

## Scope and validation limits

The starter retains weather parsing, lunar birthdays, Gregorian anniversaries, complete message-field assembly, short-poem fallback, and job failure when any recipient fails. WeChat POST requests are not automatically retried to avoid duplicate sending after a timeout.

Only the [public Chinese poetry API](https://github.com/xenv/gushici), which requires no personal API key, is retained. Historical built-in keys for other text APIs have been removed. No admin interface, database, Docker setup, or other unnecessary components have been added.

Local offline tests, packaging, empty-configuration checks, and a personal-data scan have been completed. Services and templates have not been created for new accounts, and no real GitHub send has been performed for this generic package. See the [validation notes (Chinese)](docs/validation.md).

Actions uses [checkout v5](https://github.com/actions/checkout/tree/v5), [setup-java v5](https://github.com/actions/setup-java/tree/v5), and JDK 11. Validate the workflow in your own repository.
