package io.branch.adobe.sdk;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionListener;

public class AdobeBranchExtensionListener extends ExtensionListener {
    protected AdobeBranchExtensionListener(final ExtensionApi extension, final String type, final String source) {
        super(extension, type, source);
    }

    @Override
    public void hear(final Event event) {
        // run the event processing on its own executor in the parent extension class
        getParentExtension().handleConfigurationEvent(event);
    }

    @Override
    protected AdobeBranchExtension getParentExtension() {
        return (AdobeBranchExtension)super.getParentExtension();
    }

}
