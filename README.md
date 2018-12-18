# AdobeBranchExtension

Add the power of Branch deep linking and attribute to your Adobe Marketing Cloud app. With Branch's linking platform, mobile developers and marketers can grow their mobile business with world class deep linking and attribution.

## Features
1. All events tracked with the Adobe SDK will automatically be sent to Branch without any extra work
2. All core Branch functionality is accessible

## Requirements
- Android API level 16 or higher
- Adobe Core Platform

## Example

An example app can be found in the AdobeBranchExtension-Android repository, in the `AdobeBranchExample`
project.

- [AdobeBranchExample Project](https://github.com/BranchMetrics/AdobeBranchExtension-Android/tree/master/AdobeBranchExample)
- [AdobeBranchExtension-Android Repository](https://github.com/BranchMetrics/AdobeBranchExtension-Android)

## Installation & Usage

Here's a brief outline of how to use the AdobeBranchExtension in your app:

1. You'll need to configure your app and get a Branch API key in the [Branch Metrics dashboard](https://branch.dashboard.branch.io/account-settings/app). You can read more about configuring your dashboard in the Branch docs here.

2. For application integration, you'll need to follow the instructions as described in the Branch docs here:

   - [Integrate Branch](https://docs.branch.io/pages/apps/android/)

3. Also add an app URI scheme and your Branch key to the manifest file for you app for deep linking.

   - [Configure your application with Branch key and for URI schemes](https://docs.branch.io/pages/apps/android/#configure-app)

4. In the Adobe dashboard, activate Branch and add your Branch key to your app's configuration.

   Activate Branch:

   ![Activate Branch](scripts/images/adobe-dash-install.png)

5. Add the AdobeBranchExtension to your app's build.gradle.

        implementation 'io.branch.sdk.android:adobebranchextension:1.+'

6. Register the Branch `AdobeBranchExtension` with `MobileCore` in `configureWithAppID`:

```
    private static final String MY_ADOBE_APP_ID = "launch-{adobe app guid}-development";

    ...
    // Initialize the AdobeBranch SDK
    AdobeBranch.getAutoInstance(this);
    
    // Initialize the Adobe SDK
    MobileCore.setApplication(this);
    MobileCore.start(new AdobeCallback () {
        @Override
        public void call(Object o) {
            MobileCore.configureWithAppID(MY_ADOBE_APP_ID);
        }
    });

```

Congratulations! With those quick and easy steps you've installed and activated the AdobeBranchExtension.

## Implementing Branch Features

Once you've added the AdobeBranchExtension and Branch, you can always use Branch features directly. You can learn about using the Branch features here, in the Branch documentation for Android.](https://docs.branch.io/pages/apps/android/)


### Automatic: Track Action and State
When you track actions and state in Adobe Launch, the action and state messages are sent to Branch too and shown on the
Branch dashboards. This allows you to track the effectiveness of deep link campaigns and viral sharing in your app's actions.

Here's an example of tracking app state via Adobe Launch:

    private void doPurchase(View view) {
        Long timestamp = System.currentTimeMillis()/1000;

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(AdobeBranch.KEY_AFFILIATION, "Branch Metrics Company Store");
        eventData.put(AdobeBranch.KEY_COUPON, "SATURDAY NIGHT SPECIAL");
        eventData.put(AdobeBranch.KEY_CURRENCY, "USD");
        eventData.put(AdobeBranch.KEY_DESCRIPTION, model.getDescription());
        eventData.put(AdobeBranch.KEY_REVENUE, model.getPrice());
        eventData.put(AdobeBranch.KEY_SHIPPING, 0.99);
        eventData.put(AdobeBranch.KEY_TAX, (model.getPrice() * 0.077));
        eventData.put(AdobeBranch.KEY_TRANSACTION_ID, UUID.randomUUID().toString());

        eventData.put("category", "Arts & Entertainment");
        eventData.put("product_id", model.getId());
        eventData.put("sku", "sku-be-doo");
        eventData.put("timestamp", timestamp.toString());

        eventData.put("custom1", "Custom Data 1");
        eventData.put("custom2", "Custom Data 2");

        Event newEvent = new Event.Builder("PURCHASE",
                "com.adobe.eventType.generic.track",
                "com.adobe.eventSource.requestContent")
                .setEventData(eventData).build();

        // dispatch the analytics event
        MobileCore.dispatchEvent(newEvent, this);
    }


## Author

Andy Peterson apeterson@branch.io

## License

AdobeBranchExtension is available under the MIT license. See the LICENSE file for more info.

## Developer Resources

- [Branch Documentation](https://docs.branch.io/)
- [Branch Dashboard](https://dashboard.branch.io/)
- [Adobe Mobile SDK V5](https://launch.gitbook.io/marketing-mobile-sdk-v5-by-adobe-documentation/release-notes)
- [Adobe Branch Mobile Extension UI Plugin](https://github.com/BranchMetrics/adobe-branch-mobile-plugin)
- [Adobe Mobile SDK V5 Docs](https://launch.gitbook.io/marketing-mobile-sdk-v5-by-adobe-documentation/build-your-own-extension)