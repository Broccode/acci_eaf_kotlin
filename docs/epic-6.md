# Epic 6: Internationalization (i18n) - Core Functionality & Control Plane Integration
>
> This document is a granulated shard from the main "ACCI-EAF-PRD.md" focusing on "Epic 6: Internationalization (i18n) - Core Functionality & Control Plane Integration".

*Description:* Implements the `eaf-internationalization` module providing capabilities for loading translation files, language-dependent formatting, and language switching. Includes Control Plane UI features for tenants to manage their custom languages and translations.
*Value:* Enables multilingual applications and tenant-specific language customization.

**Story 6.1: EAF Core i18n Mechanism - Translation File Loading & Message Resolution**

* **As an** EAF Developer, **I want** the EAF to provide a robust mechanism for loading translation files (e.g., Java ResourceBundles) and resolving internationalized messages based on a user's locale, **so that** applications built on the EAF can easily support multiple languages.
* **Acceptance Criteria (ACs):**
    1. The `eaf-internationalization` module defines a clear strategy for organizing and loading translation files (e.g., `.properties` files in UTF-8 format, per language/locale, using standard Java `ResourceBundle` conventions). The strategy supports loading bundles from the application's classpath and potentially from external directories for later extensions.
    2. The EAF provides a central, easy-to-use service (e.g., a facade around Spring's `MessageSource`) that applications can use to retrieve localized messages by key and optional parameters.
    3. The mechanism supports parameterized messages (e.g., "Hello {0}, you have {1} new messages.") using the `java.text.MessageFormat` standard or an equivalent, secure method.
    4. A clearly defined fallback mechanism is implemented: If a translation for the requested locale and key does not exist, an attempt is made to fall back to a more general language (e.g., from `de_CH` to `de`) and finally to a configurable primary language of the EAF (e.g., English or German). If no key is found even there, a defined placeholder (e.g., `???key_name???`) or the key itself is returned, and a warning is logged.
    5. The setup and usage of the i18n mechanism (including file organization, key conventions, usage of the message service) are detailed for EAF application developers in the documentation.
    6. Comprehensive unit tests verify message resolution for various locales (including variants like `de_DE`, `de_CH`), correct fallback behavior for missing keys or languages, and correct processing of parameterized messages. The behavior with malformed resource bundle files (e.g., incorrect character encoding, syntax errors) is defined (e.g., error on startup, logged warning).
    7. The performance of message resolution is optimized (e.g., through caching of loaded ResourceBundles) to avoid significant overhead during frequent calls.

**Story 6.2: EAF Support for Locale-Specific Data Formatting**

* **As an** EAF Developer, **I want** the EAF to facilitate locale-specific formatting for numbers, dates, times, and currencies, **so that** data is presented correctly according to the user's language and cultural preferences.
* **Acceptance Criteria (ACs):**
    1. The `eaf-internationalization` module provides utility classes or clear guidance and examples for integrating and using standard Java/Kotlin libraries (e.g., `java.text.NumberFormat`, `java.text.DateFormat`, `java.time.format.DateTimeFormatter`, `java.util.Currency`) for locale-dependent formatting of numbers (decimal numbers, percentages).
    2. Corresponding utility classes/guidance are provided for locale-dependent formatting of dates and times (short, medium, long format). The use of `java.time` is recommended.
    3. Corresponding utility classes/guidance are provided for locale-dependent formatting of currency amounts (including currency symbol and correct positioning).
    4. Examples and documentation show EAF application developers how to securely use these formatting capabilities in conjunction with the user's current locale (determined via Story 6.3), both in backend logic (e.g., for generating reports) and in frontend components (possibly by providing locale info for client-side formatting).
    5. Error handling for invalid locale inputs to formatting functions is defined (e.g., fallback to default locale, exception).
    6. The documentation points out potential pitfalls in international formatting (e.g., different calendar systems, timezone issues – although in-depth timezone handling may go beyond pure formatting).

**Story 6.3: User Language Preference Management & Switching in EAF Applications**

* **As an** EAF Developer, **I want** the EAF to provide a simple way for applications to manage a user's language preference and allow users to switch languages, **so that** the application UI can be displayed in the user's chosen language.
* **Acceptance Criteria (ACs):**
    1. The EAF provides a mechanism to determine the current user's locale. The order of precedence is: 1. Explicit user selection (persistently stored), 2. Browser's `Accept-Language` header, 3. Configured default locale of the application/EAF.
    2. The EAF supports persisting the user's explicit language preference (e.g., as part of the user profile in the database (see `LocalUser` entity) or as a long-lived cookie/`localStorage` entry). The chosen method is secure and respects privacy.
    3. The EAF offers reusable components, server-side helper methods, or clear guidance for implementing a language switcher UI element (e.g., dropdown menu with available languages) in EAF-based web applications (especially for the Control Plane UI).
    4. Changing the language preference by the user results in the application interface (on next navigation or by dynamically reloading affected components) displaying texts and, if applicable, formatted data in the newly selected language, using the i18n mechanism from Story 6.1 and 6.2.
    5. The currently active locale is easily accessible server-side for the entire duration of a request (e.g., via a `LocaleContextHolder` or similar).
    6. If a user's persisted language preference points to an unsupported or invalid language, a defined fallback occurs (e.g., to the application's default language), and the user may be informed.

**Story 6.4: Control Plane API for Tenant-Specific Language Management**

* **As a** Tenant Administrator (via Control Plane API), **I want** to be able to add new custom languages for my tenant and manage their availability, **so that** my instance of an EAF-based application can support languages beyond the default set.
* **Acceptance Criteria (ACs):**
    1. Backend API endpoints (e.g., under `/api/controlplane/tenants/{tenantId}/languages`) are provided in the EAF and secured by appropriate permissions for tenant administrators.
    2. The endpoints support the following operations with JSON Payloads:
        * `POST /languages`: Adds a new custom language code (e.g., "fr-CA-custom", adhering to BCP 47 format or a defined convention) for the tenant specified in the path. Validates the language code for format and uniqueness per tenant.
        * `GET /languages`: Lists all languages available to the tenant (default application languages and custom languages added by the tenant), including their activation status.
        * `PUT /languages/{langCode}`: Updates properties of a custom language (e.g., display name, activation status `enabled/disabled`). Default application languages cannot be modified via this API (except possibly their activation/deactivation status for the tenant).
        * `DELETE /languages/{langCode}`: Removes a *custom* language for the tenant. Default application languages cannot be deleted. Confirmation is recommended. Deleting a language with existing translations leads to defined behavior (e.g., archival of translations or error if actively used).
    3. The EAF provides persistent storage (e.g., a dedicated DB table `tenant_languages`) for these tenant-specific language configurations, linked to the `tenantId`.
    4. The EAF's core i18n mechanism (Story 6.1) can recognize these tenant-defined languages and (if translations are present, see Story 6.5) consider them for message resolution for users of that tenant.
    5. All changes to a tenant's language settings are recorded in the audit log.
    6. Validation errors (e.g., invalid language code, attempt to delete a standard language) lead to clear HTTP 4xx error messages.

**Story 6.5: Control Plane API & UI for Tenant-Specific i18n Text Translation**

* **As a** Tenant Administrator (via Control Plane), **I want** to provide and manage my own translations for the application's i18n text keys for my custom languages (and potentially override default translations), **so that** I can fully localize the application for my users.
* **Acceptance Criteria (ACs):**
    1. Backend API endpoints (e.g., under `/api/controlplane/tenants/{tenantId}/languages/{langCode}/translations`) allow tenant administrators to submit and manage translations for all relevant i18n keys for their (tenant-activated) languages.
    2. The API supports at least:
        * `GET /`: Lists all i18n keys of the base application, ideally with the base language's default translations and the current tenant-specific translations for the specified `{langCode}`. Pagination and filtering by key or translation status (translated/untranslated/overridden) are supported.
        * `PUT /{messageKey}`: Creates or updates the tenant-specific translation for a given `{messageKey}` and the specified `{langCode}`. The request body contains the translation text. Validates inputs (e.g., for maximum length, preventing XSS via server-side sanitization if texts could be directly interpreted as HTML – however, it's better to let the frontend handle interpretation and store only plain text here).
        * `DELETE /{messageKey}`: Removes a tenant-specific translation for a key (causes fallback to the application's default translation).
    3. The EAF provides persistent storage (e.g., a dedicated DB table `tenant_translations` with `tenantId`, `langCode`, `messageKey`, `translationText`) for these tenant-specific translations.
    4. The EAF's core i18n message resolution mechanism (Story 6.1) prioritizes tenant-specific translations (from this storage) over default application translations when resolving a message for a given tenant and locale.
    5. A section in the Control Plane UI (React-Admin based, accessible to tenant administrators with appropriate permissions) is developed to:
        * List languages configured for the tenant (from Story 6.4).
        * Allow selection of a language for editing translations.
        * Display a paginated and filterable list of i18n keys, alongside the default translation (from the base application) and the input field/display for the tenant-specific translation.
        * Provide an intuitive interface (e.g., inline edit fields, save buttons per entry or for a group) for tenants to input, edit, and delete translations for each key in the selected language. Changes are saved via the API (see ACs 1-2).
        * Provide visual feedback on save status (saved, error, pending).
    6. The UI for translation management is robust and user-friendly, even with a large number of text keys (e.g., through efficient pagination, search/filter functions by key or content).
    7. Changes to tenant-specific translations are recorded in the audit log.
    8. The UI clearly indicates whether a translation is tenant-specific or from the application standard.

---
