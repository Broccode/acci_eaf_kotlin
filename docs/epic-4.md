# Epic 4: Control Plane UI - Phase 1 (Tenant & Basic User Management)
>
> This document is a granulated shard from the main "ACCI-EAF-PRD.md" focusing on "Epic 4: Control Plane UI - Phase 1 (Tenant & Basic User Management)".

*Description:* Develops the initial version of the Control Plane UI (React-Admin based) providing administrative capabilities for managing tenants (CRUD) and managing local users & their roles within those tenants (leveraging APIs from Epic 2 & 3).
*Value:* Provides a usable interface for core administrative tasks.

**Story 4.1: Control Plane UI Shell & Login**

* **As a** Control Plane Administrator, **I want** a basic UI shell (navigation, layout) for the Control Plane and a login screen, **so that** I can securely access the administrative functionalities.
* **Acceptance Criteria (ACs):**
    1. A new React application is initialized and configured for the Control Plane UI (e.g., using Create React App, Vite, or a similar established toolchain, with TypeScript as the standard language). The project includes basic linting and formatting rules.
    2. The UI uses an established component framework that approximates the style of React-Admin (e.g., Material-UI, Ant Design, or directly React-Admin components) to ensure a professional, functional, and consistent look and feel.
    3. A login page is implemented with input fields for username/email and password, and a login button. It securely calls the backend authentication API (from Story 3.3, possibly adapted for Control Plane Admins). CSRF protection is implemented if applicable.
    4. Upon successful login, an access token (e.g., JWT) is securely stored on the client (e.g., in `localStorage` or `sessionStorage` with XSS prevention considerations, or as an `HttpOnly` cookie if supported by the backend and suitable for the architecture). The user is redirected to a main dashboard or landing page. The "logged-in" state is persistent across page reloads (within token validity).
    5. Basic navigation (e.g., persistent sidebar, header with user menu and logout button) is present. Navigation displays only sections for which the logged-in administrator has permissions (RBAC-driven).
    6. The UI is primarily optimized for desktop browsers (current versions of Chrome, Firefox, Edge). Basic responsive display ensures core information is viewable on tablets without critical display errors or loss of functionality.
    7. Failed login attempts (e.g., invalid credentials, server error, locked account) are displayed to the user with a clear but non-detailed (to prevent enumeration) error message. Repeated failed attempts may trigger a short client-side delay before a new attempt is possible.
    8. A "password forgotten" flow is **not** part of this MVP phase; users are directed to a manual administrative process for password resets.
    9. The UI traps global JavaScript errors and unhandled API response errors (e.g., 5xx server errors), displaying a generic, user-friendly error message to prevent UI "freezing" or a blank white page. Client-side error logging (e.g., Sentry.io or simple `console.error` with potential backend logging) is considered.
    10. A logout button is present, invalidates the local session/token, and redirects the user to the login page.

**Story 4.2: UI for Tenant Management (CRUD)**

* **As a** Control Plane Administrator, **I want** a UI section to manage tenants (List, Create, View, Edit, Deactivate/Activate), **so that** I can perform tenant administration tasks visually.
* **Acceptance Criteria (ACs):**
    1. A "Tenant Management" section is securely accessible via the UI navigation (only for administrators with appropriate permissions).
    2. A data grid/table displays a list of tenants. Displayed columns include at least Tenant ID (possibly shortened/linked), Name, Status (e.g., Active, Inactive), and Creation Date. The table supports client-side or server-side pagination for large tenant lists, sorting by most columns, and free-text search/filtering (e.g., by name, status).
    3. A form (e.g., in a modal or separate page) allows creating new tenants (calling the API from Story 2.4). The form includes client-side validation for required fields (e.g., name) and data formats according to API specifications before sending the request. API error messages (e.g., "Name already exists," validation errors) are displayed understandably to the user, directly associated with the relevant fields or as a global form message.
    4. A read-only detail view (e.g., accessible by clicking a tenant in the list) allows displaying all relevant information of a selected tenant.
    5. A form allows editing existing tenants (e.g., name, status) (calling the API from Story 2.4). Client-side validation and contextual error handling for API errors are also implemented here. Fields that cannot be changed (e.g., `tenantId`) are not editable.
    6. Actions (e.g., buttons in the table row, context menu items, or buttons in the detail view) allow deactivating/activating tenants, preceded by a confirmation dialog (to prevent unintended actions). The tenant's status is correctly updated in the list after the action without requiring a manual refresh.
    7. User interactions are intuitive and follow the "React-Admin" style (e.g., clear buttons for primary and secondary actions, consistent form layouts, informative tooltips).
    8. Loading states (e.g., when fetching data for the table, saving changes in forms) are visually indicated to the user (e.g., via loading indicators, disabling buttons during action). Success and error messages after actions are clearly displayed (e.g., as "toast" notifications).

**Story 4.3: UI for Local User Management within a Tenant**

* **As a** Control Plane Administrator (or Tenant Administrator, if the UI later supports tenant-level admin logins and the logged-in user has appropriate rights for the selected tenant), **I want** a UI section to manage local users within a selected tenant (List, Create, View, Edit, Manage Status, Reset Password), **so that** tenant user administration can be done visually.
* **Acceptance Criteria (ACs):**
    1. Within a tenant's detail view or a dedicated "User Management" section (clearly scoped to a tenant and only accessible when a tenant is selected/in context), a data grid/table displays the local users of that tenant (columns: e.g., Username, Email, Status, Creation Date). Pagination, filtering, and sorting are supported.
    2. Forms and actions allow creating new local users (Username, Email, initial Password – password entered masked and only used for API submission), viewing user details, editing user information (e.g., Email, Status), and triggering password resets (calling APIs from Story 3.2). Validation, error handling, and loading states are implemented as described in Story 4.2.
    3. User status (Active, Locked, Disabled) can be managed via the UI, including confirmation dialogs for critical status changes.
    4. When creating a user or resetting a password, server-defined password strength policies (if any) are indicated client-side (e.g., as a tooltip) and validated server-side; corresponding API error messages are displayed.
    5. The UI never displays plaintext or hashed passwords at any time.
    6. Attempting to create a user whose username already exists within the tenant context results in a clear error message.
    7. User interactions are intuitive and consistent with the rest of the Control Plane.

**Story 4.4: UI for RBAC Management (Role Assignment within a Tenant)**

* **As a** Control Plane Administrator (or Tenant Administrator with appropriate rights), **I want** a UI section to manage role assignments to users within a selected tenant, **so that** access control can be configured visually.
* **Acceptance Criteria (ACs):**
    1. A UI section (e.g., in the user detail view of a local user or service account, or as a separate "Roles" tab/section) allows viewing the roles available for the current tenant (both system-wide applicable roles and tenant-specific ones, if supported later).
    2. For a selected user/service account, their currently assigned roles are clearly displayed.
    3. For a selected user/service account, roles can be assigned or unassigned from a list of available roles for the tenant (e.g., via a multi-select box, a list with checkboxes, drag-and-drop interface). Changes call the appropriate APIs from Story 3.4.
    4. An explicit save action with a confirmation dialog is required before changes to role assignments take effect.
    5. *(MVP Focus for Phase 1):* Creating/editing roles themselves and assigning permissions to roles is **not** done via the UI in this phase. The UI focuses solely on assigning *existing, predefined* roles to users/service accounts.
    6. User interactions are intuitive and error-tolerant (e.g., attempting to assign a non-existent role should be prevented by UI selection).
    7. Errors during saving of role assignments (e.g., API error, concurrent modification) are clearly communicated to the user. Loading states are displayed.

**Story 4.5: UI for Service Account Management within a Tenant**

* **As a** Control Plane Administrator (or Tenant Administrator with appropriate rights), **I want** a UI section to manage service accounts within a selected tenant (List, Create, View, Edit, Manage Status, Manage Credentials, View/Manage Expiration), **so that** M2M access can be administered visually.
* **Acceptance Criteria (ACs):**
    1. Within a tenant's detail view or a dedicated "Service Account Management" section (scoped to a tenant), a data grid/table displays the service accounts of that tenant (columns e.g., Client ID, Description, Status, Creation Date, Expiration Date). Pagination, filtering, and sorting are supported.
    2. Forms and actions allow creating new service accounts (with display of default expiration date and option for adjustment, if applicable), viewing details (including Client ID and expiration date, but *without* the client secret), editing information (e.g., description, status, expiration date), and managing credentials (e.g., option to regenerate client secret – the new secret is then displayed *once* and must be copied securely by the admin; Client ID is visible) (calling APIs from the updated Story 3.5).
    3. The status (Active, Disabled) and expiration date of service accounts can be managed via the UI.
    4. Visual indicators (e.g., color coding, icons) in the list and detail view highlight service accounts that are expiring soon or already expired.
    5. User interactions are intuitive. Copying the Client ID and the once-displayed Client Secret is facilitated by UI elements (e.g., "Copy" button).
    6. Clear warnings and confirmation dialogs are displayed before a service account is deleted/deactivated or a client secret is rotated/regenerated.
    7. Validation, error handling, and loading states are implemented as described in Story 4.2.

---
