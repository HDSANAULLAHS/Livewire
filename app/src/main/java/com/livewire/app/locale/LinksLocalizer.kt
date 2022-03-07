package com.livewire.app.locale

import com.livewire.app.locale.models.Language
import com.livewire.app.locale.models.Region

open class LinksLocalizer(val localeManager: LocaleManager) {

    val vistHdWebsiteLink: String
        get() {

            val region = localeManager.getCurrentRegion()
            val language = localeManager.getCurrentLanguage()

            return when {
                region == Region.USA ->
                    "https://www.harley-davidson.com/us/en/index.html?source_cd=hdrideapp"
                else ->
                    "https://www.harley-davidson.com/us/en/index.html?source_cd=hdrideapp"
            }
        }

    val motorcycleLink: String
        get() {

            val region = localeManager.getCurrentRegion()
            val language = localeManager.getCurrentLanguage()

            return when {
                region == Region.USA ->
                    "https://www.harley-davidson.com/us/en/motorcycles/index.html?source_cd=hdrideapp_motorcycles"

                else ->
                    "https://www.harley-davidson.com/us/en/motorcycles/index.html?source_cd=hdrideapp"
            }
        }

    val testRideLink: String?
        get() {

            val region = localeManager.getCurrentRegion()
            val language = localeManager.getCurrentLanguage()

            return when {
                region == Region.USA ->
                    "https://www.harley-davidson.com/us/en/tools/test-ride.html?source_cd=hdrideapp_test_rides"
                else ->
                    null
            }
        }

    val learnToRideLink: String?
        get() {

            val region = localeManager.getCurrentRegion()

            return when (region) {
                Region.USA ->
                    "https://www.harley-davidson.com/us/en/content/motorcycle-training/learn-to-ride.html?source_cd=hdrideapp_learn_to_ride"
                else ->
                    null
            }
        }

    val rentABikeLink: String?
        get() {

            val region = localeManager.getCurrentRegion()
            val language = localeManager.getCurrentLanguage()

            return when {
                region == Region.USA ->
                    "https://www.harley-davidson.com/us/en/content/rent-a-bike.html?source_cd=hdrideapp_rent_a_bike"

                else ->
                    null
            }
        }


    val museumLink: String?
        get() {

            val region = localeManager.getCurrentRegion()
            val language = localeManager.getCurrentLanguage()

            return when {
                region == Region.USA ->
                    "https://www.harley-davidson.com/us/en/museum.html?source_cd=hdrideapp_museum"
                else ->
                    null
            }
        }


    val clothingStoreLink: String
        get() {

            val region = localeManager.getCurrentRegion()
            val language = localeManager.getCurrentLanguage()

            return when {
                region == Region.USA ->
                    "https://www.harley-davidson.com/us/en/index.html?source_cd=hdrideapp_online_store"
                else ->
                    "https://www.harley-davidson.com/store?source_cd=hdrideapp"
            }
        }

    val privacyPolicyLink: String = "https://www.livewire.com/documents/privacy-policy"
    val contactUsLink: String = "https://www.harley-davidson.com/contact"
    val tosLink: String = "https://www.livewire.com/documents/terms-of-use"

    val weCareLink: String
        get() {

            val region = localeManager.getCurrentRegion()
            val language = localeManager.getCurrentLanguage()

            return when {
                region == Region.USA ->
                    "https://www.harley-davidson.com/us/en/footer/utility/we-care-about-you.html?source_cd=hdrideapp_we_care_about_you"
                else ->
                    "https://www.harley-davidson.com/us/en/footer/utility/we-care-about-you.html?source_cd=hdrideapp"
            }
        }

    val hereMapsTermsOfServiceLink: String
        get() {

            val region = localeManager.getCurrentRegion()
            val language = localeManager.getCurrentLanguage()

            return when {
                region == Region.USA ->
                    "https://legal.here.com/us-en/terms"

                else ->
                    "https://legal.here.com/us-en/terms"
            }
        }

    val hereMapsPrivacyPolicyLink: String
        get() {

            val region = localeManager.getCurrentRegion()
            val language = localeManager.getCurrentLanguage()

            return when {
                region == Region.USA ->
                    "https://legal.here.com/us-en/privacy/policy"
                else ->
                    "https://legal.here.com/us-en/privacy/policy"
            }
        }

    val connectTermsOfUse: String
        get() {

            return when (localeManager.getCurrentLanguage()) {
                Language.ENGLISH_USA ->
                    "file:///android_asset/terms_of_use/connect_terms_of_use_en_us.html"
                else ->
                    "file:///android_asset/terms_of_use/connect_terms_of_use_en_us.html"
            }
        }


    val onboardingTermsOfUseFileLink: String
        get() {

            return when (localeManager.getCurrentLanguage()) {
                Language.ENGLISH_USA ->
                    "file:///android_asset/terms_of_use/terms_of_use_en_us.html"
            }
        }

    val bicycleDashboardLink: String
        get() = when (localeManager.getCurrentLanguage()) {
            // TODO
            Language.ENGLISH_USA ->
                "file:///android_asset/bicycle_dashboard/placeholder.html"
            else ->
                "file:///android_asset/bicycle_dashboard/placeholder.html"
        }
}
