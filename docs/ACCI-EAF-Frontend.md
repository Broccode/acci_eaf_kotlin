Okay, here is the complete "ACCI EAF Control Plane UI Frontend Architecture Document" (Version 0.1.0) in English:

# ACCI EAF Control Plane UI Frontend Architecture Document

## Table of Contents

- [Introduction](https://www.google.com/search?q=%23introduction)
- [Overall Frontend Philosophy & Patterns](https://www.google.com/search?q=%23overall-frontend-philosophy--patterns)
- [Detailed Frontend Directory Structure](https://www.google.com/search?q=%23detailed-frontend-directory-structure)
- [Component Breakdown & Implementation Details](https://www.google.com/search?q=%23component-breakdown--implementation-details)
  - [Component Naming & Organization](https://www.google.com/search?q=%23component-naming--organization)
  - [Template for Component Specification](https://www.google.com/search?q=%23template-for-component-specification)
- [State Management In-Depth](https://www.google.com/search?q=%23state-management-in-depth)
  - [Store Structure / Slices (for Context & Potential Zustand)](https://www.google.com/search?q=%23store-structure--slices-for-context--potential-zustand)
  - [Key Selectors (for Context & Potential Zustand)](https://www.google.com/search?q=%23key-selectors-for-context--potential-zustand)
  - [Key Actions / Reducers / Thunks (for Context & Potential Zustand)](https://www.google.com/search?q=%23key-actions--reducers--thunks-for-context--potential-zustand)
- [API Interaction Layer](https://www.google.com/search?q=%23api-interaction-layer)
  - [Client/Service Structure](https://www.google.com/search?q=%23clientservice-structure)
  - [Error Handling & Retries (Frontend)](https://www.google.com/search?q=%23error-handling--retries-frontend)
- [Routing Strategy](https://www.google.com/search?q=%23routing-strategy)
  - [Route Definitions](https://www.google.com/search?q=%23route-definitions)
  - [Route Guards / Protection](https://www.google.com/search?q=%23route-guards--protection)
- [Build, Bundling, and Deployment](https://www.google.com/search?q=%23build-bundling-and-deployment)
  - [Build Process & Scripts](https://www.google.com/search?q=%23build-process--scripts)
  - [Key Bundling Optimizations](https://www.google.com/search?q=%23key-bundling-optimizations)
  - [Deployment to CDN/Hosting](https://www.google.com/search?q=%23deployment-to-cdnhosting)
- [Frontend Testing Strategy](https://www.google.com/search?q=%23frontend-testing-strategy)
  - [Component Testing](https://www.google.com/search?q=%23component-testing)
  - [Feature/Flow Testing (UI Integration)](https://www.google.com/search?q=%23featureflow-testing-ui-integration)
  - [End-to-End UI Testing Tools & Scope](https://www.google.com/search?q=%23end-to-end-ui-testing-tools--scope)
- [Accessibility (AX) Implementation Details](https://www.google.com/search?q=%23accessibility-ax-implementation-details)
- [Performance Considerations](https://www.google.com/search?q=%23performance-considerations)
- [Internationalization (i18n) and Localization (l10n) Strategy](https://www.google.com/search?q=%23internationalization-i18n-and-localization-l10n-strategy)
- [Feature Flag Management](https://www.google.com/search?q=%23feature-flag-management)
- [Frontend Security Considerations](https://www.google.com/search?q=%23frontend-security-considerations)
- [Browser Support and Progressive Enhancement](https://www.google.com/search?q=%23browser-support-and-progressive-enhancement)
- [Change Log](https://www.google.com/search?q=%23change-log)

## Introduction

This document details the technical architecture specifically for the frontend of the ACCI EAF Control Plane UI. It complements the main ACCI EAF Architecture Document and the UI/UX guidelines derived from the ACCI EAF Product Requirements Document (PRD). This document details the frontend architecture and **builds upon the foundational decisions** (e.g., overall tech stack, CI/CD, primary testing tools) defined in the main ACCI EAF Architecture Document (`docs/ACCI-EAF-Architecture.md`). **Frontend-specific elaborations or deviations from general patterns must be explicitly noted here.** The goal is to provide a clear blueprint for frontend development, ensuring consistency, maintainability, and alignment with the overall system design and user experience goals.

- **Link to Main Architecture Document (REQUIRED):** `docs/ACCI-EAF-Architecture.md`
- **Link to UI/UX Specification (REQUIRED if exists):** `docs/ACCI-EAF-PRD.md` (especially the "User Interaction and Design Goals" section)
- **Link to Primary Design Files (Figma, Sketch, etc.) (REQUIRED if exists):** Not yet specified - React-Admin serves as functional and stylistic inspiration. If specific design files exist or are created, link them here.
- **Link to Deployed Storybook / Component Showcase (if applicable):** Not applicable yet

## Overall Frontend Philosophy & Patterns

This section describes the core architectural decisions and patterns chosen for the ACCI EAF Control Plane UI frontend. This aligns with the "Definitive Tech Stack Selections" in the main architecture document and considers implications from the overall system architecture.

- **Framework & Core Libraries:**
  - **React:** v19.1
  - **React-Admin:** v5.8.1
        These are derived from the 'Definitive Tech Stack Selections' in the main Architecture Document. This section elaborates on *how* these choices are applied specifically to the frontend.
- **Component Architecture:**
  - Primary reliance on **React-Admin's component suite** (e.g., `<List>`, `<Datagrid>`, `<Edit>`, `<SimpleForm>`, `<TextInput>`) for standard CRUD operations and UI structures.
  - Custom components will be developed for functionalities or UI elements not adequately covered by React-Admin.
  - A clear distinction will be made between **Presentational (Dumb) Components** (primarily concerned with UI) and **Container (Smart) Components** (managing logic, state, and data fetching, often leveraging React-Admin hooks or custom hooks).
  - Reusable UI elements (e.g., custom buttons, specialized input fields beyond React-Admin's scope) will be developed and potentially managed in a local component library or Storybook.
- **State Management Strategy:**
  - Primarily leverage **React-Admin's built-in state management capabilities (Ra-Store)**, which internally uses React Context and Redux principles for resource management, UI state, and optimistic updates.
  - For global state not directly tied to React-Admin resources (e.g., global UI settings, notifications not handled by React-Admin, complex cross-feature state), **React Context API** will be the first choice.
  - If more complex global state scenarios arise that are not well-suited for React Context, **Zustand** might be considered as a lightweight alternative to a full Redux setup, due to its simplicity and hook-based API. This will be evaluated if specific needs arise.
        Referenced from main Architecture Document and detailed further in "State Management In-Depth" section.
- **Data Flow:**
  - Unidirectional data flow as facilitated by React and React-Admin.
  - React-Admin's **Data Provider** will be the primary interface for data fetching, caching, and mutations with the `eaf-controlplane-api`.
  - UI components will receive data via props (passed down from React-Admin resource controllers or custom container components) or access it via React-Admin's hooks (e.g., `useListController`, `useEditController`) or custom hooks consuming Context or other state management solutions.
- **Styling Approach:** **React-Admin Theming & Customization (likely Material UI based) + CSS Modules**.
  - Configuration File(s): Theming object for React-Admin, potentially `*.module.css` files.
  - Key conventions:
    - Leverage React-Admin's theming capabilities to achieve the "neutral" branding requirement from the PRD. This involves customizing the default theme (often based on Material UI).
    - For custom components or significant overrides not manageable via the theme, **CSS Modules** will be used. CSS Module files (e.g., `MyComponent.module.css`) will be co-located with their respective components to ensure local scope and avoid style conflicts.
    - Global styles, if absolutely necessary, will be minimal and managed in a central file imported at the application root.
- **Key Design Patterns Used:**
  - **React Hooks:** Extensively used for state, side effects, and custom logic encapsulation.
  - **React-Admin specific patterns:** Data Providers, Resource Controllers, Custom Routes, Authentication Providers.
  - **Higher-Order Components (HOCs):** As used by React-Admin or for custom cross-cutting concerns if necessary, though hooks are generally preferred.
  - **Service Pattern for API calls:** While React-Admin's Data Provider abstracts most direct API calls, any direct or complex interactions outside its scope would be encapsulated in service modules.
  - **Provider Pattern:** For React Context usage.

## Detailed Frontend Directory Structure

The frontend application for the ACCI EAF Control Plane UI is located in the `controlplane-ui/` directory of the monorepo. Vite is used as the build tool. The following ASCII diagram represents the recommended folder structure within `controlplane-ui/`:

```plaintext
controlplane-ui/
├── public/                     # Static assets served directly by the web server (e.g., favicons, manifest.json).
│   └── locales/                # Language files for i18n, if loaded statically.
│       ├── en.json
│       └── de.json
├── src/                        # Main application source code.
│   ├── App.tsx                 # Main application component; setup of React-Admin, router, theme provider, etc.
│   ├── main.tsx                # Application entry point; renders the App component.
│   ├── vite-env.d.ts           # Type definitions for Vite environment variables.
│   │
│   ├── assets/                 # Static assets imported into components (images, fonts, etc.).
│   │   └── logo.svg
│   │
│   ├── components/             # Globally reusable UI components not specific to a resource.
│   │   ├── common/             # General, atomic UI elements (e.g., custom button, special badges).
│   │   │   └── BrandedHeader.tsx
│   │   └── layout/             # Components for page layout (e.g., custom menus, extended AppBar features).
│   │       └── CustomMenu.tsx
│   │
│   ├── features/               # Modules for each main React-Admin resource or custom feature (e.g., tenants, users).
│   │   ├── tenants/            # Example: Feature module for "Tenants".
│   │   │   ├── TenantList.tsx  # React-Admin List component for tenants.
│   │   │   ├── TenantEdit.tsx  # React-Admin Edit component for tenants.
│   │   │   ├── TenantCreate.tsx# React-Admin Create component for tenants.
│   │   │   ├── TenantShow.tsx  # React-Admin Show component (optional).
│   │   │   └── components/     # Components specific to the tenants feature.
│   │   │       └── TenantStatusChip.tsx
│   │   ├── users/              # Feature module for "Users" (similar structure to tenants).
│   │   ├── licenses/           # Feature module for "Licenses" (similar structure).
│   │   └── i18nAdmin/          # Feature module for i18n management in the UI (if applicable).
│   │       ├── LanguageList.tsx
│   │       └── TranslationEditPage.tsx
│   │
│   ├── pages/                  # Custom pages/views not directly tied to React-Admin resources.
│   │   ├── DashboardPage.tsx
│   │   └── GlobalSettingsPage.tsx
│   │
│   ├── hooks/                  # Globally reusable custom React Hooks.
│   │   └── useAppConfiguration.ts
│   │
│   ├── providers/              # React-Admin Provider configurations.
│   │   ├── dataProvider.ts     # Configured React-Admin Data Provider for the eaf-controlplane-api.
│   │   ├── authProvider.ts     # Configured React-Admin Auth Provider.
│   │   └── i18nProvider.ts     # Configured React-Admin i18n Provider (can also import language files).
│   │
│   ├── router/                 # Routing configuration.
│   │   └── customRoutes.tsx    # Definitions for custom routes outside of React-Admin resources.
│   │
│   ├── store/                  # State management for non-React-Admin related global states (e.g., with React Context or Zustand).
│   │   ├── themeContext.tsx
│   │   └── notificationStore.ts # Example for Zustand state
│   │
│   ├── styles/                 # Styling-related files.
│   │   ├── theme.ts            # React-Admin Theme configuration object (customizing Material UI).
│   │   └── global.css          # Minimal global styles (CSS resets, base fonts, if not covered by theme).
│   │
│   ├── types/                  # Global TypeScript type definitions and interfaces.
│   │   ├── index.d.ts          # Aggregates types or global extensions.
│   │   └── react-admin.d.ts    # Extensions for React-Admin types if needed.
│   │
│   └── utils/                  # Global utility functions and constants.
│       ├── helpers.ts
│       └── constants.ts
│
├── tests/                      # Test-specific files and configurations.
│   ├── setupTests.ts           # Setup file for tests (e.g., Jest/Vitest configuration, global mocks).
│   └── e2e/                    # End-to-End tests with Playwright.
│       └── tenants.spec.ts
│
├── index.html                  # Main HTML file (Vite convention).
├── package.json                # Project dependencies and scripts.
├── vite.config.ts              # Vite build tool configuration file.
├── tsconfig.json               # TypeScript compiler configuration.
├── postcss.config.js           # PostCSS configuration (for Autoprefixer, etc.).
└── README.md                   # Readme for the frontend project.
```

### Notes on Frontend Structure

- **Influence of Vite:** The use of Vite as a build tool influences this structure as follows:
  - `index.html` is located in the root directory of the `controlplane-ui` project.
  - The `public/` directory is used for static assets that are served directly and unchanged.
  - `vite.config.ts` contains the build, development server, and optimization configurations.
  - Environment variables accessible in client code MUST be prefixed with `VITE_` (e.g., `VITE_API_BASE_URL`).
- **Modularity through Features:** Structuring by `features/` (or alternatively `resources/`) promotes modularity and simplifies the management of code related to specific React-Admin resources or application areas. Each feature directory is largely self-contained.
- **Component Co-location:** Component tests (`*.test.tsx` or `*.spec.tsx` for unit and integration tests with Jest/Vitest and React Testing Library) are placed directly next to the files they test or in a `__tests__` subdirectory within the `src/` tree. E2E tests (`tests/e2e/`) are separate.
- **CSS Modules:** CSS Module files (`*.module.css`) are placed directly next to the components they style to ensure local scope and encapsulation of styles.
- **Provider Encapsulation:** React-Admin specific providers (`dataProvider`, `authProvider`, `i18nProvider`) are encapsulated in their own `providers/` directory to keep their configuration central and clear.
- **Strict Adherence:** AI agents and developers MUST strictly adhere to this defined structure. New files MUST be placed in the appropriate directory based on these descriptions.

## Component Breakdown & Implementation Details

This section describes the conventions and templates for defining UI components. The detailed specification for most feature-specific components will emerge during the implementation of user stories. AI agents and developers MUST use the "Template for Component Specification" below whenever a new, significant custom component is identified.

### Component Naming & Organization

- **Component Naming Convention:** **PascalCase for file names and component names (e.g., `TenantForm.tsx`, `UserProfileCard.tsx`)**. All component files MUST follow this convention.
- **Organization:**
  - **React-Admin Components:** The primary approach is to use and configure React-Admin's extensive component library (e.g., `<List>`, `<Datagrid>`, `<Edit>`, `<SimpleForm>`, `<TextInput>`, `<ReferenceInput>`, etc.) for standard CRUD views and operations. These components are used directly within the feature modules (e.g., `src/features/tenants/TenantList.tsx`) and configured via their props.
  - **Globally Reusable Custom Components:** Custom components that are reusable aplikasi-wide and not specific to a single feature or resource are placed in `src/components/common/` (for atomic UI elements) or `src/components/layout/` (for structural layout components).
  - **Feature-Specific Custom Components:** Custom components used exclusively within a specific feature or resource are co-located in the `components/` subdirectory of the respective feature module (e.g., `src/features/tenants/components/TenantStatusChip.tsx`).
  - **Presentational vs. Container Components:** This distinction is handled as follows:
    - React-Admin Resource components (e.g., `<List>`, `<Edit>`) often act as container components, managing data fetching and business logic via hooks (`useListController`, `useEditController`).
    - Custom components should, where sensible, be designed as Presentational Components that receive data and callbacks via props. More complex logic or state management can be encapsulated in custom hooks, which are then used by "Smart" wrapper components or directly by feature components.
- **Guidelines for Creating Custom Components:**
  - Custom components are created when:
    - React-Admin does not provide a suitable component for the required UI/UX.
    - A specific, heavily branded, or interactive UI requirement cannot be achieved by configuring standard React-Admin components.
    - Complex, reusable UI logic needs to be encapsulated and is required across multiple parts of a feature or the application.
  - These custom components MUST follow the "Template for Component Specification."

### Template for Component Specification

For each significant *custom* UI component identified from the UI/UX specification and design files (or functional necessity), the following details MUST be provided. This template is not intended for the standard configuration of React-Admin components unless a complex wrapper with its own logic and props is being created. Repeat this subsection for each such component. The level of detail MUST be sufficient for an AI agent or developer to implement it with minimal ambiguity.

#### Component: `{ComponentName}` (e.g., `AuditLogEntry`, `ComplexFilterPanel`)

- **Purpose:** {Briefly describe what this component does and its role in the user interface. MUST be clear and concise.}
- **Source File(s):** {e.g., `src/features/auditing/components/AuditLogEntry.tsx`. MUST be the exact path.}
- **Visual Reference:** {Link to a specific Figma frame/component, a Storybook page, or a detailed description/sketch if no formal design exists. REQUIRED.}
- **Props (Properties):**
    {List each prop the component accepts. For each prop, all columns in the table MUST be filled.}

    | Prop Name     | Type                                                                 | Required? | Default Value | Description                                                                                                                               |
    | :------------ | :------------------------------------------------------------------- | :-------- | :------------ | :---------------------------------------------------------------------------------------------------------------------------------------- |
    | `exampleProp` | `string`                                                             | Yes       | N/A           | The ID of the entity to display. MUST be a valid UUID.                                                                             |
    | `variant`     | `'compact' \| 'full'`                                                | No        | `'full'`      | Controls the display mode of the component.                                                                                                  |
    | `{anotherProp}` | `{Specific primitive, imported type, or inline interface/type definition}` | {Yes/No}  | {If any} | {MUST clearly state the prop's purpose and any constraints, e.g., 'Must be a positive integer.'}                               |

- **Internal State (if any):**
    {Describe any significant internal state the component manages. List only state that is *not* derived from props or global state. If the state is complex, consider if it should be managed by a custom hook or a global state management solution instead.}

    | State Variable  | Type      | Initial Value | Description                                                                |
    | :-------------- | :-------- | :------------ | :------------------------------------------------------------------------- |
    | `isLoading`     | `boolean` | `false`       | Tracks if data for the component is loading.                      |
    | `{anotherState}`| `{type}`  | `{value}`     | {Description of the state variable and its purpose.}                      |

- **Key UI Elements / Structure:**
    {Provide a pseudo-HTML or JSX-like structure representing the component's DOM. Include important conditional rendering logic if applicable. **This structure dictates the primary output for the AI agent.**}

    ```html
    <div class="audit-log-entry {variant === 'compact' ? 'compact-styles' : 'full-styles'}">
      <span class="timestamp">{formattedTimestamp(entry.timestamp)}</span>
      <span class="user">{entry.user.name}</span>
      {variant === 'full' && <p class="details">{entry.details}</p>}
    </div>
    ```

- **Events Handled / Emitted:**
  - **Handles:** {e.g., `onClick` on a detail button (triggers `onViewDetails` prop).}
  - **Emits:** {If the component emits custom events/callbacks not covered by props, describe them with their exact signature. e.g., `onExpand: (payload: { entryId: string; isExpanded: boolean }) => void`}
- **Actions Triggered (Side Effects):**
  - **State Management:** {e.g., "Dispatches `uiSlice.actions.showNotification({ message: 'Action performed' })` from `src/store/notificationStore.ts`. Action payload MUST match the defined action creator."}
  - **API Calls:** {Specify which service/function from the "API Interaction Layer" is called (usually not directly from pure Presentational Components). e.g., "Calls `auditService.fetchDetails(entryId)` from `src/features/auditing/services/auditService.ts`."}
- **Styling Notes:**
  - {MUST reference specific Design System component names (e.g., "Uses `<Button variant='primary'>` from Material UI via React-Admin Theme") OR specify CSS module class names to be applied (e.g., "Container uses `styles.auditEntryContainer`. Title uses `styles.entryTitle` from `AuditLogEntry.module.css`."). Any dynamic styling logic based on props or state MUST be described. AI agent should prioritize the use of CSS modules for custom components.}
- **Accessibility Notes:**
  - {MUST list specific ARIA attributes and their values (e.g., `aria-label="Audit log entry for action X"`), required keyboard navigation behavior (e.g., "Entire component is focusable via Tab, and details can be expanded/collapsed with Enter/Space if interactive."), and any focus management requirements (e.g., "If this component opens a modal, focus MUST be trapped inside. On modal close, focus returns to the triggering element.").}

-----

*Repeat the above template for each significant custom component.*

-----

## State Management In-Depth

This section expands on the State Management strategy outlined in the "Overall Frontend Philosophy & Patterns" section. The main burden of state management for CRUD operations and resources is handled internally by React-Admin. This section focuses on the overarching strategy and the management of states that go beyond that.

- **Chosen Solution:**

    1. **React-Admin Internal State (Ra-Store):** The primary solution for all states directly related to resources managed by React-Admin (e.g., tenants, users, licenses). This includes data fetching, caching, optimistic updates, list filters/sorting, selection states, and the state of edit/create forms. Developers primarily interact with this via React-Admin Hooks (e.g., `useListController`, `useEditController`, `useCreateController`, `useDataProvider`) and resource configuration.
    2. **React Context API:** For global state not directly tied to React-Admin resources and of medium complexity. Use cases include global UI settings (theme preferences, if dynamic and not solvable via React-Admin Theme), application-wide notifications not covered by React-Admin's standard notification system, or shared state within a specific, complex custom feature or page that does not follow the React-Admin resource model.
    3. **Zustand (Potentially):** If more complex global state scenarios arise that are not resource-specific and would be cumbersome to manage with React Context (e.g., very complex user session details beyond basic authentication, state management for a multi-step wizard in a custom non-React-Admin process), Zustand could be considered as a lightweight, hook-based alternative to a full Redux installation. The need for this will be evaluated on a case-by-case basis.
    4. **Local Component State (`useState`, `useReducer`):** By default, for UI-specific, ephemeral state within individual components (e.g., state of form inputs before submission to React-Admin, state of dropdown menus, UI toggles affecting only one component).

- **Decision Guide for State Location:**

  - **Ra-Store (React-Admin internal):** **MUST** be used for all data and UI states directly related to entities defined via `<Resource>`. This is the default and is largely managed automatically by React-Admin.
  - **React Context API (`src/store/contexts/`):** **MUST** be used for:
    - Theming variables not part of the static React-Admin theme.
    - Global application-wide configurations or states that are read by many components but rarely changed.
    - State primarily passed down a specific component tree (e.g., within a complex custom layout or a multi-step form page not corresponding to a React-Admin `Resource`).
  - **Zustand (if needed, in `src/store/slices/`):** May be considered for:
    - Complex, non-resource-related global states that require frequent updates or are modified by many, not directly hierarchically connected components.
    - Management of states whose logic should be decoupled from pure UI components (e.g., application-wide notification queues with advanced logic).
  - **Local Component State (`useState`, `useReducer`):** **MUST** be the default choice for all other UI-related state that does not meet the criteria for React Context, Zustand, or Ra-Store.

### Store Structure / Slices (for Context & Potential Zustand)

Since Ra-Store encapsulates its state management internally, this section primarily refers to the structure for React Context and potential use of Zustand.

- **React Context API Structure:**

  - Contexts are defined in the `src/store/contexts/` directory.
  - Each context typically consists of:
    - A file creating the context itself (`React.createContext()`).
    - A Provider component that manages the state (often with `useState` or `useReducer`) and provides the context value.
    - A custom hook (e.g., `useThemeSettings`) to easily consume the context value.
  - **Example: `src/store/contexts/ThemeContext.tsx`** (for a simple light/dark mode toggle in addition to the RA theme)

        ```typescript
        import React, { createContext, useContext, useState, useMemo } from 'react';

        type ThemeMode = 'light' | 'dark';
        interface ThemeContextType {
          mode: ThemeMode;
          toggleMode: () => void;
        }

        const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

        export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
          const [mode, setMode] = useState<ThemeMode>('light'); // Default mode

          const toggleMode = () => {
            setMode((prevMode) => (prevMode === 'light' ? 'dark' : 'light'));
          };

          const value = useMemo(() => ({ mode, toggleMode }), [mode]);

          return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
        };

        export const useTheme = (): ThemeContextType => {
          const context = useContext(ThemeContext);
          if (context === undefined) {
            throw new Error('useTheme must be used within a ThemeProvider');
          }
          return context;
        };
        ```

        The `ThemeProvider` would then wrap the `App` component or parts of it.

- **Zustand Store Structure (Exemplary, if needed):**

  - Zustand stores would be placed in `src/store/slices/` (e.g., `src/store/slices/notificationStore.ts`).
  - **Example: `notificationStore.ts`** (for an advanced, application-wide notification system)

        ```typescript
        import { create } from 'zustand';

        interface Notification {
          id: string;
          message: string;
          type: 'info' | 'success' | 'warning' | 'error';
          autoHideDuration?: number;
        }

        interface NotificationState {
          notifications: Notification[];
          addNotification: (notification: Omit<Notification, 'id'>) => void;
          removeNotification: (id: string) => void;
          clearAllNotifications: () => void;
        }

        export const useNotificationStore = create<NotificationState>((set) => ({
          notifications: [],
          addNotification: (notification) =>
            set((state) => ({
              notifications: [...state.notifications, { ...notification, id: new Date().toISOString() }],
            })),
          removeNotification: (id) =>
            set((state) => ({
              notifications: state.notifications.filter((n) => n.id !== id),
            })),
          clearAllNotifications: () => set({ notifications: [] }),
        }));
        ```

        This store could then be used in UI components via the `useNotificationStore` hook.

### Key Selectors (for Context & Potential Zustand)

- **React-Admin (Ra-Store):** Selectors are implicitly part of the hooks provided by React-Admin (e.g., `useListContext().data`, `useEditContext().record`). There are no explicit, user-defined Redux-style selectors for accessing the Ra-Store.
- **React Context:** The "selector" is direct access to the values provided by the context provider and consumed via the custom hook (e.g., `const { mode } = useTheme();`).
- **Zustand:**
  - Simple selectors are functions passed to the hook:

        ```typescript
        const notifications = useNotificationStore((state) => state.notifications);
        const notificationCount = useNotificationStore((state) => state.notifications.length);
        ```

  - For more complex or memoized selectors (to avoid unnecessary re-renders if only irrelevant parts of the store change), `zustand/middleware` with `subscribeWithSelector` or manual memoization with `useMemo` in the component can be considered.

### Key Actions / Reducers / Thunks (for Context & Potential Zustand)

- **React-Admin (Ra-Store):** Actions are typically triggered by interacting with React-Admin's UI components (e.g., saving a form) or by calling functions of the `dataProvider` (e.g., `dataProvider.update()`, `dataProvider.create()`). React-Admin manages the internal "reducers" and "thunks" (sagas for optimistic rendering).
- **React Context:**
  - If the provider uses `useState`, "actions" are the `setState` functions exported by the provider (e.g., `toggleMode` in the `ThemeContext` example).
  - If the provider uses `useReducer`, actions are passed to the `dispatch` function and processed by the reducer function.
- **Zustand:**
  - "Actions" are functions defined within the `create` call that modify the state via the `set` function (e.g., `addNotification`, `removeNotification` in the `notificationStore` example).
  - Asynchronous actions can be implemented directly within these functions:

        ```typescript
        // Example of an asynchronous action in a Zustand store
        // interface AppConfigState {
        //   config: AppConfig | null;
        //   isLoading: boolean;
        //   fetchAppConfig: () => Promise<void>;
        // }
        // fetchAppConfig: async () => {
        //   set({ isLoading: true });
        //   try {
        //     const response = await apiClient.get('/app-config'); // Assuming apiClient is available
        //     set({ config: response.data, isLoading: false });
        //   } catch (error) {
        //     console.error("Failed to fetch app config", error);
        //     set({ isLoading: false }); // Error handling here or in UI
        //   }
        // },
        ```

## API Interaction Layer

This section describes how the frontend communicates with the `eaf-controlplane-api` defined in the main architecture document. The primary interface for this is the React-Admin `DataProvider`.

### Client/Service Structure

- **HTTP Client Setup (for DataProvider):**

  - The React-Admin `DataProvider` will use a centrally configured HTTP client to send requests to the backend. We will consider using `Workspace` with a custom wrapper function or a lightweight library like `ky` to handle interceptors and configurations. If more complex scenarios (like automatic retries, discussed below) are broadly needed, Axios could also be considered. Configuration will be in `src/providers/httpClient.ts` (or a similar utility module).
  - **Base URL:** Will be obtained from an environment variable `VITE_API_BASE_URL` (e.g., `VITE_API_BASE_URL=/controlplane/api/v1`). This is in line with the Vite convention.
  - **Default Headers:**
    - `Content-Type: 'application/json'`
    - `Accept: 'application/json'`
  - **Interceptors / Wrapper Logic (for the DataProvider's HTTP client):**
    - **Auth Token Injection:** The HTTP client will be configured to automatically retrieve the authentication token (JWT) from the `authProvider` (e.g., via `authProvider.getIdentity()` or a similar method that securely provides the token) and include it in the `Authorization` header of every request (e.g., `Authorization: Bearer <token>`).
    - **Error Normalization:** Before errors are returned to the React-Admin `DataProvider`, the HTTP wrapper can normalize error objects to ensure consistent error handling (e.g., extracting error messages from the API response structure).
    - **Request/Response Logging (Development):** In development environments, interceptors can be useful for logging requests and responses.

- **React-Admin Data Provider (`src/providers/dataProvider.ts`):**

  - This is the central interface for all CRUD operations and data queries for resources managed by React-Admin (tenants, users, licenses, etc.).
  - It implements the `DataProvider` interface from React-Admin, including methods like `getList`, `getOne`, `getMany`, `getManyReference`, `create`, `update`, `updateMany`, `delete`, `deleteMany`.
  - **API Mapping:** The `dataProvider` maps React-Admin method calls to the specific endpoints and REST dialect of the `eaf-controlplane-api` (according to the OpenAPI specification `docs/api/controlplane-v1.yml`). This includes:
    - Adapting pagination, sorting, and filter parameters to the format expected by the backend.
    - Correct URL generation for resources and sub-resources.
  - **Authentication Integration:** The `dataProvider` works closely with the `authProvider`. In case of API errors indicating authentication problems (e.g., 401 Unauthorized), the `authProvider` will trigger appropriate actions (e.g., logout, redirect to login page).
  - **Optimistic Updates:** React-Admin supports optimistic updates for `create`, `update`, and `delete` operations. The `dataProvider` will be implemented to leverage this feature by returning the expected (locally modified) resource immediately after a successful request, before data is re-fetched from the server. This improves perceived performance.
  - **Data Transformation:** Necessary transformations between the API's data format and the format expected by the frontend (especially by React-Admin components) will occur within the `dataProvider` methods (e.g., renaming fields, adjusting data types, preparing references).

- **Service Definitions (for Non-React-Admin API calls):**

  - For API calls that do not follow React-Admin's CRUD model or represent special operations (e.g., triggering a batch job, fetching specific dashboard data that is not a "resource"), separate service modules can be created in `src/services/` (e.g., `src/services/dashboardService.ts`).
  - These services would use the same configured `httpClient` instance (or wrapper function) to ensure consistency in authentication and error handling.
  - **Example (`src/services/tenantActionsService.ts`):**

        ```typescript
        import { httpClient } from '../providers/httpClient'; // Assuming httpClient is exported

        export const tenantActionsService = {
          activateTenant: async (tenantId: string): Promise<void> => {
            await httpClient.post(`/tenants/${tenantId}/activate`);
          },
          deactivateTenant: async (tenantId: string): Promise<void> => {
            await httpClient.post(`/tenants/${tenantId}/deactivate`);
          }
        };
        ```

### Error Handling & Retries (Frontend)

- **Global Error Handling (in React-Admin context):**

  - **DataProvider Errors:** The `dataProvider` MUST return errors (rejected Promises) in a way that React-Admin can process them. This usually means returning an object with a `status` code and a `message` (or a `body` with details). React-Admin displays these errors as notifications by default. Error messages from the API (according to API specification) will be extracted and prepared in the `dataProvider` or `httpClient` wrapper to be user-friendly.
  - **AuthProvider Errors:** The `authProvider` (`src/providers/authProvider.ts`) handles authentication and authorization errors. On a 401 error (token expired or invalid), it typically redirects the user to the login page. On a 403 error (insufficient permissions), an "Access Denied" page can be displayed or a notification issued.
  - **Global React Error Boundary:** A top-level React Error Boundary (`src/components/common/GlobalErrorBoundary.tsx`) will be implemented to catch unexpected JavaScript errors in the UI that are not direct API or authentication errors. It will display a user-friendly error message and possibly allow the user to reload the application.

- **Specific Error Handling:**

  - **React-Admin Forms:** `<SimpleForm>` and related components display validation errors returned by the `dataProvider` (in response to `create` or `update` calls) directly at the corresponding fields. The API must return structured error messages for this (e.g., `{ errors: { fieldName: 'Error message' } }`), which the `dataProvider` will format accordingly.
  - **Custom Components/Services:** Errors occurring during calls via custom services (e.g., `tenantActionsService`) must be explicitly handled in the calling components or hooks (e.g., displaying an inline error message, disabling a button, triggering a specific notification via the `notificationStore` or React-Admin's `useNotify` hook).

- **Retry Logic:**

  - By default, React-Admin does not implement automatic retry logic for `dataProvider` calls.
  - Should retry logic be necessary for specific, known-to-be-unstable, idempotent GET requests (e.g., due to network issues), it MUST be implemented in the underlying `httpClient`.
    - **Configuration (if implemented, e.g., with `axios-retry` or manual wrapper):**
      - Maximum number of retries: e.g., 2-3 attempts.
      - Conditions for retry: Only on network errors or specific 5xx server errors (e.g., 503 Service Unavailable).
      - Delay between retries: Exponential backoff (e.g., 1s, 2s, 4s).
    - **IMPORTANT:** Retry logic MAY ONLY be applied to idempotent requests (GET, PUT, DELETE under certain conditions). It is generally unsuitable for POST requests to avoid duplicate resource creation. The necessity and implementation will be reviewed on a case-by-case basis.

## Routing Strategy

This section describes how navigation and routing are handled in the ACCI EAF Control Plane UI. React-Admin internally uses React Router and provides mechanisms for defining routes for resources as well as custom pages.

- **Routing Library:**
  - **React Router v6.x:** This version is typically used and managed internally by React-Admin v5.8.1 (and newer). We will leverage the abstractions and custom route integration capabilities provided by React-Admin.
  - React-Admin automatically generates routes for each defined `<Resource>` component (e.g., `/tenants`, `/tenants/create`, `/tenants/:id`, `/tenants/:id/show`).

### Route Definitions

Most application routes are implicitly created by `<Resource>` definitions within the `<Admin>` component. For example:
`<Resource name="tenants" list={TenantList} edit={TenantEdit} create={TenantCreate} />` generates:

- `/tenants` (list view)
- `/tenants/create` (create view)
- `/tenants/:id` (edit view, default on list click)
- `/tenants/:id/show` (show view, if a `show` component is defined)

In addition to these React-Admin managed routes, custom routes may be needed for pages not directly following a CRUD model for a single resource. These are integrated via the `customRoutes` prop of the `<Admin>` component or as direct children of `<Routes>` within a custom layout structure. Definitions for these routes will be in `src/router/customRoutes.tsx`.

| Path Pattern                | Component/Page (`src/pages/...` or `src/features/...`) | Protection                                  | Notes                                                                                                |
| :-------------------------- | :------------------------------------------------------- | :------------------------------------------ | :--------------------------------------------------------------------------------------------------- |
| `/`                         | `pages/DashboardPage.tsx`                                | `Authenticated`                             | Homepage after login; displays overview/dashboard information.                                  |
| `/login`                    | (React-Admin Default or `pages/LoginPage.tsx`)         | `Public` (automatic redirect if auth.) | Login page. React-Admin redirects here if not authenticated.                                |
| `/settings`                 | `pages/GlobalSettingsPage.tsx`                           | `Authenticated`                             | Page for global application settings (if needed).                                         |
| `/profile`                  | `pages/UserProfilePage.tsx`                              | `Authenticated`                             | Page for managing one's own user profile.                                                    |
| `/i18n-administration`      | `features/i18nAdmin/TranslationManagementPage.tsx`       | `Authenticated`, `Role:[ADMIN_I18N]`        | Custom page for managing translations (per Epic 6, if not a standard RA resource). |
| `/access-denied`            | `pages/AccessDeniedPage.tsx`                             | `Authenticated` (technically)                 | Page displayed when a user attempts to access a resource without permission. |
| `{another custom path}`     | `{ComponentPath}`                                      | `{Public/Authenticated/Role:[ROLE]}`       | {Notes, parameters, purpose}                                                                      |

**Integration of custom routes (`src/App.tsx` and `src/router/customRoutes.tsx`):**
Custom routes are defined in `src/router/customRoutes.tsx` and then imported into the `<Admin>` component.

```tsx
// src/router/customRoutes.tsx
import React from 'react';
import { Route } from 'react-router-dom';
import DashboardPage from '../pages/DashboardPage';
import GlobalSettingsPage from '../pages/GlobalSettingsPage';
// ... other imports

export const customRoutes = [
  <Route key="dashboard" path="/" element={<DashboardPage />} />,
  <Route key="settings" path="/settings" element={<GlobalSettingsPage />} />,
  // ... other custom routes
];

// src/App.tsx (excerpt)
// ...
// import { customRoutes } from './router/customRoutes';
// ...
// const App = () => (
//   <Admin
//     dataProvider={dataProvider}
//     authProvider={authProvider}
//     i18nProvider={i18nProvider}
//     // customRoutes can be passed here or within a custom layout
//     // <CustomRoutes> {customRoutes} </CustomRoutes> // React-Admin v3/v4 style
//     // For React-Admin with React Router v6, it's often part of a custom layout or <Admin dashboard={DashboardPage}> and other routes as children.
//     // Alternative: A custom layout using <Routes> from React Router v6.
//     // We will use the customRoutes prop or create a custom layout that integrates them.
//     // For RA v5+ with React Router v6, it's common to use a <Layout> containing the <AdminUI> component
//     // and placing custom routes outside <AdminUI> but within the protected area.
//     // Or they can be passed as children of <Admin> if they should use the default layout:
//   >
//     <Resource name="tenants" list={TenantList} edit={TenantEdit} create={TenantCreate} />
//     {/* Custom routes using the Admin layout */}
//     {customRoutes.map(route => route)}
//     {/* ... other resources */}
//   </Admin>
// );
```

The exact integration of `customRoutes` into `<Admin>` depends on the desired layout structure. If they should use the standard React-Admin layout (sidebar, app bar), they can be passed as direct children of the `<Admin>` component (alongside `<Resource>` definitions). For fully custom behavior, they can be placed within a custom layout component.

### Route Guards / Protection

- **Authentication Guard:**

  - Authentication is primarily controlled by the `authProvider` (`src/providers/authProvider.ts`) of React-Admin. The methods `authProvider.login()`, `authProvider.logout()`, `authProvider.checkAuth()`, and `authProvider.checkError()` are central to this.
  - React-Admin automatically redirects users to the login page (configured via the `loginPage` prop of `<Admin>`, default `/login`) if `checkAuth()` returns an error or if the user is not authenticated and tries to access a protected resource or page.
  - All `<Resource>` components and all custom routes rendered within the `<Admin>` context (see above) are automatically protected by this mechanism.

- **Authorization Guard (Role-based Access Control - RBAC):**

  - React-Admin does not offer a declarative RBAC solution for routes "out-of-the-box." Authorization is typically handled at the component level or by conditionally rendering UI elements based on user permissions.
  - The `authProvider` MUST implement a `getPermissions()` method (e.g., `authProvider.getPermissions()`) that returns the current user's roles or permissions (e.g., `['ROLE_ADMIN', 'TENANT_MEMBER_VIEW']`).
  - **For React-Admin Resources:**
    - The visibility of `<Resource>` components in navigation or access to specific actions (edit, create, delete) can be controlled by fetching permissions in the `authProvider` and adjusting the UI or Resource props accordingly. React-Admin Enterprise Edition offers advanced components for this.
    - Within list, edit, or show views, actions or fields can be shown/hidden based on user permissions.
  - **For Custom Routes/Pages:**
    - One option is to create a wrapper component `ProtectedRoute` that accepts the required permissions as a prop and checks them:

            ```tsx
            // src/router/ProtectedRoute.tsx
            import React from 'react';
            import { usePermissions } from 'react-admin';
            import { Navigate, useLocation } from 'react-router-dom';
            import LoadingPage from '../pages/LoadingPage'; // Assuming a loading component

            interface ProtectedRouteProps {
              children: JSX.Element;
              requiredPermissions?: string[]; // e.g., ['ROLE_ADMIN']
            }

            const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredPermissions }) => {
              const { isLoading, permissions } = usePermissions();
              const location = useLocation();

              if (isLoading) {
                return <LoadingPage />; // Or another loading indicator
              }

              if (!requiredPermissions || requiredPermissions.length === 0) {
                return children; // No specific permissions required
              }

              const hasRequiredPermissions = requiredPermissions.every(rp => permissions?.includes(rp));

              if (!hasRequiredPermissions) {
                // Redirect user to "Access Denied" page or dashboard with a message
                return <Navigate to="/access-denied" state={{ from: location }} replace />;
              }

              return children;
            };

            export default ProtectedRoute;
            ```

            This component could then be used in `customRoutes.tsx`:
            `<Route path="/admin-only" element={<ProtectedRoute requiredPermissions={['ROLE_ADMIN']}><AdminOnlyPage /></ProtectedRoute>} />`
    - Alternatively, permission checks can be performed directly in the page component, which then redirects to `/access-denied` or displays an appropriate message if needed.

## Build, Bundling, and Deployment

This section describes the build process, bundling optimization strategies, and deployment details for the ACCI EAF Control Plane UI frontend. These details complement the "Infrastructure and Deployment Overview" section in the main architecture document.

### Build Process & Scripts

- **Build Tool:** **Vite** is used as the primary build tool and development server. Configuration is done via `vite.config.ts`.

- **Key Build Scripts (from `package.json`):**
    The following scripts are typically defined in `package.json`:

  - `"dev"`: `vite`
    - Starts the Vite development server with Hot Module Replacement (HMR) and fast load times.
  - `"build"`: `vite build`
    - Creates an optimized production build of the application in the `dist/` directory. This includes transpilation, bundling, minification, and asset hash generation for caching.
  - `"preview"`: `vite preview`
    - Starts a local web server that serves the content of the `dist/` directory. Useful for testing the production build locally before deployment.
  - `"test"`: `jest` (or `vitest run` if switching to Vitest)
    - Runs unit and integration tests (per Tech Stack selection: Jest with React Testing Library).
  - `"test:e2e"`: `playwright test`
    - Runs end-to-end tests with Playwright.
  - `"lint"`: `eslint . --ext .js,.jsx,.ts,.tsx --fix`
    - Checks code for linting errors according to ESLint rules and attempts to fix them automatically.
  - `"format"`: `prettier --write "src/**/*.{js,jsx,ts,tsx,css,md}"`
    - Formats code automatically according to Prettier rules.

- **Environment Configuration Management:**

  - Vite uses `.env` files to manage environment variables. The conventions are:
    - `.env`: Default values, version controlled.
    - `.env.local`: Local overrides, *not* version controlled (in `.gitignore`).
    - `.env.development`: Values specific to the development environment.
    - `.env.production`: Values specific to the production environment.
  - Environment variables that should be available in client code MUST be prefixed with `VITE_` (e.g., `VITE_API_BASE_URL`, `VITE_APP_TITLE`).
  - Access in code via `import.meta.env.VITE_VARIABLE_NAME`.
  - AI agents and developers MUST ensure that no sensitive data or environment-specific values are hardcoded. All such values MUST be provided via the defined environment variable mechanism.

### Key Bundling Optimizations

Vite is optimized for performance by default and implements many of these strategies automatically.

- **Code Splitting:**
  - Vite performs route-level code splitting by default (dynamic imports for page components).
  - For manual code splitting of large, non-critical components or libraries not needed immediately on first load, JavaScript's dynamic `import()` syntax (or `React.lazy` with `<Suspense>`) MUST be used. Example: `const HeavyComponent = React.lazy(() => import('./components/HeavyComponent'));`
- **Tree Shaking:**
  - Automatically applied by Vite in production builds (via Rollup) to remove unused code from bundles. This requires code to be written in ES modules and side effects to be minimized.
- **Lazy Loading (Components, Images, etc.):**
  - **Components:** As described above, `React.lazy` in conjunction with `<Suspense>` for components not immediately visible or necessary.
  - **Images:** Use the `loading="lazy"` attribute for `<img>` tags by default to delay image loading until they enter the viewport. For more advanced image optimizations (e.g., responsive images, different formats), specific components or libraries may be considered if necessary.
- **Minification & Compression:**
  - **Minification:** Vite automatically minifies JavaScript/TypeScript (with esbuild/Terser), CSS (with Lightning CSS or esbuild), and HTML in production builds.
  - **Compression (Gzip, Brotli):** Asset compression is typically handled by the hosting platform or CDN (e.g., Vercel, Netlify, AWS CloudFront) at runtime or during deployment. The build process itself usually does not generate `.gz` or `.br` files.

### Deployment to CDN/Hosting

- **Target Platform:** {This value will be taken from the main architecture document (section "Infrastructure and Deployment Overview"). Common platforms for React/Vite applications include Vercel, Netlify, AWS S3/CloudFront, Azure Static Web Apps. Please enter here once definitive.}
- **Deployment Trigger:** {This value will be taken from the main architecture document (CI/CD Pipeline section). Typically a Git push to the `main` or `production` branch via a CI/CD pipeline (e.g., GitHub Actions, GitLab CI).}
- **Asset Caching Strategy:**
  - **Immutable Assets:** JavaScript and CSS bundles generated by Vite with content hashes in their filenames (e.g., `app.[hash].js`) MUST be served with long `Cache-Control` headers (e.g., `public, max-age=31536000, immutable`). This ensures browsers cache these files aggressively.
  - **`index.html`:** The main HTML file MUST be served with shorter `Cache-Control` headers or `Cache-Control: no-cache` / `Cache-Control: public, max-age=0, must-revalidate`. This ensures users always get the latest version of the application, which then loads the versioned assets.
  - **Other static assets (in `public/` folder):** The caching strategy depends on the volatility of the assets. Immutable assets can have long cache times, while changing assets require shorter times or validation mechanisms.
  - **Configuration:** Cache headers are typically configured on the hosting platform or CDN.

## Frontend Testing Strategy

This section builds upon the "Overall Testing Strategy" of the main architecture document and details the specific aspects of frontend testing for the ACCI EAF Control Plane UI. The tools specified in the main document and the initial prompt for the Design Architect are Jest, React Testing Library, and Playwright.

- **Link to Main Overall Testing Strategy:** {Reference to the corresponding section in the main architecture document `docs/ACCI-EAF-Architecture.md#overall-testing-strategy`} (Please adjust the exact link/anchor if necessary).

### Component Testing

- **Scope:** Testing individual React components in isolation. This includes both reusable UI elements (from `src/components/`) and more specific components within feature modules (e.g., `src/features/tenants/components/TenantStatusChip.tsx`).
- **Tools:**
  - **Jest (v29.7.0):** As a test runner, assertion library, and for mocking functionalities.
  - **React Testing Library (RTL) (v16.3.x):** For rendering components in a test environment and interacting with them in a way that simulates user behavior. The focus is on testing component behavior from the user's perspective, not implementation details.
- **Focus:**
  - **Rendering:** Correct display of the component with various props (including edge cases and optional props).
  - **User Interactions:** Simulation of user interactions such as clicks, inputs, form submissions (`fireEvent` or `@testing-library/user-event` from RTL).
  - **Event Emission / Callback Handling:** Verification that callbacks are called correctly and custom events are triggered with the correct parameters.
  - **Accessibility (AX):** Basic AX checks with `jest-axe` can be integrated to ensure components do not have obvious WCAG violations (see also "Accessibility (AX) Implementation Details" section).
  - **Snapshot Testing:** MUST be used sparingly and with clear justification (e.g., for very stable, purely presentational components with complex but fixed DOM structure). Explicit assertions about the presence and content of elements are preferred.
- **Location:** Test files (`*.test.tsx` or `*.spec.tsx`) are co-located directly next to the component files they test in the `src/` directory or in a `__tests__` subdirectory within the component folder.

    ```
    // Example structure:
    // src/components/common/MyButton.tsx
    // src/components/common/MyButton.test.tsx
    ```

- **Code Coverage:** A code coverage of at least {e.g., 70-80%, threshold to be defined} for component tests is targeted. Coverage reports will be generated by Jest and can be integrated into the CI pipeline.

### Feature/Flow Testing (UI Integration)

- **Scope:** Testing the interaction of multiple components within a feature or a small user flow on a page. This may include testing a complete form, interaction between a list and a detail area, or navigation within a clearly defined feature module. API calls and global state changes are typically mocked.
- **Tools:** Same tools as for component tests (Jest and React Testing Library). Test setup will be more complex, potentially requiring mock providers for React Router, React-Admin (`<Admin dataProvider={mockDataProvider} ...>`), context providers, or Zustand stores.
- **Focus:**
  - Data flow between the involved components.
  - Conditional rendering based on interactions and state changes.
  - Correct UI updates in response to user actions and mocked service responses.
  - Navigation within the tested feature (e.g., from a list view to a detail view of an item within the mock).
- **Location:** Similar to component tests, these tests can be located next to the main components of a feature or in a dedicated test directory within the feature module.

### End-to-End UI Testing Tools & Scope

- **Tools:** **Playwright (v1.52.x)** (per Tech Stack selection). Playwright enables cross-browser testing and provides robust mechanisms for interacting with web pages.
- **Scope (Frontend Focus):** 3-5 critical end-to-end user journeys from a UI perspective MUST be covered. These test the application as a whole, interact with a (potentially mocked or dedicated test) backend API, and validate the entire user flow.
    Examples of User Journeys:
    1. **User Login and Dashboard Display:** Successful login, verification of correct redirection to the dashboard, and display of basic dashboard elements.
    2. **Tenant Management (Create & Edit):** Creating a new tenant via the form, verifying its display in the list, opening for editing, changing data, saving, and validating the changes.
    3. **User Assignment to a Tenant:** Creating a user and assigning them to an existing tenant, verifying the correct linkage.
    4. **License Activation (if UI flow exists):** Going through the UI process to activate a license for a tenant.
    5. **Navigation and Basic UI Consistency:** Verification of main navigation elements, accessibility of important pages, and consistency of header/footer/layout.
- **Test Data Management for UI:**
  - A consistent test data strategy is crucial for E2E tests. Options:
    - **API Mocking Layer:** Using tools like Mock Service Worker (MSW) to simulate API responses in the browser during E2E tests. This offers high control and speed.
    - **Dedicated Test Backend Instance:** Using a separate backend instance running with a defined set of test data or reset before each test run.
    - **Test Accounts:** Using predefined test user accounts with specific roles and data.
  - The chosen strategy must ensure tests are reproducible and do not fail due to changing data. API mocking with MSW or a backend seeding strategy is recommended to start.
- **Page Object Model (POM):** To improve the maintainability and readability of E2E tests, the Page Object Model (or a similar abstraction like the Screenplay Pattern) SHOULD be used. Selectors and interaction logic for specific pages or reusable UI areas are encapsulated in separate classes/modules.
- **Location:** E2E tests are located in the `controlplane-ui/tests/e2e/` directory.

## Accessibility (AX) Implementation Details

Accessibility is an integral part of the development of the ACCI EAF Control Plane UI. The goal is to achieve WCAG 2.1 AA compliance to ensure the application can be used by as many people as possible, including those with disabilities. The UI/UX specification (derived from the PRD) serves as the basis for AX requirements. React-Admin already provides a good foundation for accessibility, as it is often based on Material UI, which in turn emphasizes AX.

- **Semantic HTML:**

  - **Mandate:** Developers and AI agents MUST use semantically correct HTML5 elements. `<div>` and `<span>` elements are to be used only for layout purposes or when no more appropriate semantic element exists. Native elements like `<nav>`, `<aside>`, `<main>`, `<article>`, `<section>`, `<button>`, `<input type="...">`, `<label>`, `<table>`, etc., are preferred as they can be better interpreted by assistive technologies (AT).
  - React-Admin components generally use semantic HTML. This is particularly important for custom components.

- **ARIA Implementation (Accessible Rich Internet Applications):**

  - **Principle:** ARIA attributes should only be used when semantic HTML alone is insufficient to make the role, state, or properties of a component understandable to AT (Rule of ARIA: "No ARIA is better than bad ARIA").
  - **React-Admin:** Many React-Admin components (especially form fields and interaction elements) already come with correct ARIA attributes.
  - **Custom Components:** For complex custom components (e.g., custom dropdowns, modals, tab interfaces not provided by React-Admin or an underlying UI library like Material UI), the corresponding ARIA patterns from the WAI-ARIA Authoring Practices Guide (APG) MUST be implemented. This includes:
    - Correct `role` attributes (e.g., `role="dialog"`, `role="tablist"`, `role="tab"`, `role="tabpanel"`).
    - State attributes like `aria-expanded`, `aria-selected`, `aria-haspopup`, `aria-disabled`, `aria-hidden`, `aria-invalid`.
    - Property attributes like `aria-label`, `aria-labelledby`, `aria-describedby` (especially for form fields without visible labels or with additional descriptions).
    - `aria-live` for dynamic content changes (e.g., notifications, loading states).
  - All icons and purely visual elements that carry meaning MUST have a textual alternative via `aria-label` or visually hidden text if no visible label is present. Decorative icons require `aria-hidden="true"`.

- **Keyboard Navigation:**

  - **Mandate:** All interactive elements of the application MUST be operable exclusively by keyboard. This includes links, buttons, form fields, menus, and custom interactive components.
  - **Focus Order:** The focus order when navigating with the Tab key MUST be logical and intuitive, following the visual arrangement of elements.
  - **Interaction Patterns:** Standard keyboard interactions for HTML elements (e.g., Space/Enter for buttons, arrow keys for radio button groups) MUST work. For custom components, the keyboard interaction patterns specified in the ARIA APG MUST be implemented (e.g., arrow keys for custom sliders, tabs; Escape to close modals).
  - **Focus Indicator:** A clearly visible focus indicator MUST be present for all focusable elements. This is typically provided by the browser or the underlying UI library, but it must be ensured that it is not suppressed by custom styles.

- **Focus Management:**

  - **Modals and Dialogs:** When a modal or dialog is opened, focus MUST be set to the first focusable element within the modal or to the modal itself. Focus MUST be trapped within the modal (Focus Trap) until it is closed. Upon closing, focus MUST return to the element that triggered the modal.
  - **Dynamic Content Changes:** For significant changes in page content (e.g., after a search, loading new data sections) or route changes, focus SHOULD be moved to the beginning of the new content area or to an appropriate heading to help screen reader users orient themselves.
  - **Notifications:** For `aria-live` regions displaying notifications, care must be taken that they do not unnecessarily interrupt the user.

- **Testing Tools for AX:**

  - **Browser Developer Tools:** Integrated accessibility inspectors (e.g., in Chrome, Firefox, Edge).
  - **Axe DevTools (Browser Extension):** For automated tests and manual checks during development.
  - **`@axe-core/react` or `jest-axe`:** Integration of Axe-Core into component tests (Jest) to detect basic AX violations early. Tests SHOULD fail on new WCAG 2.1 AA violations.

        ```javascript
        // Example for jest-axe in a component test
        // import { render } from '@testing-library/react';
        // import { axe, toHaveNoViolations } from 'jest-axe';
        // expect.extend(toHaveNoViolations);

        // it('should have no axe violations', async () => {
        //   const { container } = render(<MyComponent />);
        //   const results = await axe(container);
        //   expect(results).toHaveNoViolations();
        // });
        ```

  - **Lighthouse (in Chrome DevTools):** For audits of the entire page, including accessibility.
  - **Manual Tests:**
    - **Keyboard Navigation:** Complete testing of all interactive elements using only the keyboard.
    - **Screen Reader Tests:** Verification of critical user journeys with common screen readers (e.g., NVDA for Windows, VoiceOver for macOS, TalkBack for Android).

- **Alignment with UI/UX Specification:** These technical implementation details MUST meet the AX requirements from the UI/UX specification (derived from the PRD), especially regarding contrast ratios (at least 4.5:1 for normal text, 3:1 for large text and UI components), responsive designs, and understandable language.

## Performance Considerations

Good performance is crucial for a positive user experience of the ACCI EAF Control Plane UI. This section describes specific strategies and techniques for optimizing frontend performance. Many of these aspects are already positively influenced by the choice of Vite as a build tool and React-Admin as a framework but require conscious application and monitoring.

- **Image Optimization:**

  - **Formats:** Modern image formats like WebP SHOULD be preferred as they are often smaller than traditional formats (JPEG, PNG) at comparable quality. For icons and simple graphics, vector graphics (SVG) ARE to be used as they are losslessly scalable and usually very small.
  - **Responsive Images:** For images displayed in different sizes on different viewports, the `<picture>` element or the `srcset` attribute of the `<img>` tag SHOULD be used to allow the browser to select the most appropriate image size.
  - **Lazy Loading:** The `loading="lazy"` attribute for `<img>` tags MUST be used by default to delay the loading of images until they enter the user's visible area. This significantly improves the initial page load time.
  - **Compression:** Images MUST be optimized and compressed before uploading (e.g., with tools like ImageOptim, Squoosh, or automated build processes).
  - **Implementation Mandate:** Wherever possible, images SHOULD be managed via components that encapsulate optimizations like lazy loading and possibly `srcset`. SVGs are to be preferred for icons and logos.

- **Code Splitting & Lazy Loading (reiterate from Build section if needed):**

  - **Impact:** Reduces the size of the initial JavaScript bundle, leading to faster application load times (Time to Interactive - TTI). Vite performs automatic route-level code splitting.
  - **Implementation Mandate:** `React.lazy` with `<Suspense>` MUST be used for components not needed immediately on the first render, especially for:
    - Components on routes that are not the initial route.
    - Large or complex components that only become visible through user interaction (e.g., modals, complex form sections).
    - Third-party modules or libraries needed only for specific functionalities.

- **Minimizing Re-renders:**

  - Unnecessary re-renders of React components can impair performance.
  - **Implementation Mandate:**
    - `React.memo` MUST be used for functional components that render frequently with the same props to prevent unnecessary re-renders.
    - Props should be kept stable. Directly passing new object or array literals, as well as inline functions as props in render methods, SHOULD be avoided if it can cause unnecessary re-renders of child components. `useCallback` and `useMemo` hooks are suitable tools for this.
    - The data structure in the state (React Context, Zustand, or React-Admin managed state) SHOULD be designed so that components only subscribe to the parts of the state relevant to them, minimizing re-renders on irrelevant state changes (Zustand and React-Admin often handle this well through their selector/hook mechanisms).
    - When working with lists, a stable `key` prop SHOULD always be used for each list item.

- **Debouncing/Throttling:**

  - For event handlers that can be triggered frequently (e.g., text input in search fields, window resizing, scroll events).
  - **Implementation Mandate:**
    - **Debouncing** (triggering the function only after a pause in events) MUST be used for actions like API calls on search field inputs.
    - **Throttling** (limiting the execution frequency) MAY be used for event handlers like scroll or resize listeners if they perform complex calculations.
    - Utility functions (e.g., from `lodash.debounce` / `lodash.throttle` or custom hooks) can be used for this. Wait times (debounce/throttle delay) should be chosen appropriately (e.g., 300-500ms for debouncing search queries).

- **Virtualization (for long lists):**

  - React-Admin's `<Datagrid>` and `<SimpleList>` render all items of a page by default. For very large lists (e.g., hundreds or thousands of entries on one page, if pagination were client-side or page size is very large), this can affect performance.
  - **Implementation Mandate:** If performance issues arise when displaying very long lists (typically \>100-200 items without server-side pagination or with very large, client-side loaded datasets), the use of virtualization libraries like `react-window` or `TanStack Virtual` MUST be considered. These render only the visible items, thus significantly improving render performance and memory usage. React-Admin Enterprise may offer virtualized components.

- **Caching Strategies (Client-Side):**

  - **HTTP Caching:** As described in the "Build, Bundling, and Deployment" section, correct HTTP cache configuration for assets is crucial.
  - **React-Admin DataProvider Caching:** React-Admin itself implements a form of caching for resources to avoid repeated requests for already loaded data. This will be utilized.
  - **Service Workers:** As PWA functionality is not considered necessary, the implementation of a service worker for caching strategies will not be pursued initially.

- **Performance Monitoring Tools:**

  - **Browser Developer Tools:**
    - **Performance Tab:** For analyzing JavaScript execution times, rendering performance, and identifying bottlenecks.
    - **Network Tab:** For analyzing load times and asset sizes.
    - **Lighthouse Audit (integrated):** For an overall assessment of performance, accessibility, best practices, and SEO.
  - **React Developer Tools (Browser Extension):** Especially the profiler, to identify and analyze component re-renders.
  - **WebPageTest / GTmetrix:** External tools for analyzing load performance under various conditions.
  - **CI Integration:** Lighthouse scores or other performance metrics MAY be integrated into the CI pipeline to detect performance regressions early.

## Internationalization (i18n) and Localization (l10n) Strategy

The ACCI EAF Control Plane UI must be multilingual to cater to different user groups. Epic 6 ("i18n Control Plane Integration") in the PRD describes the need for the Control Plane itself to be internationalized and to allow management of translations for the EAF's end applications. This section focuses on the i18n implementation of the *Control Plane UI itself*.

- **Requirement Level:** Required. Base languages are German (de-DE) and English (en-US). The architecture must be designed so that other languages can be easily added if needed.

- **Chosen i18n Library/Framework:**

  - **React-Admin `i18nProvider`:** React-Admin offers a robust `i18nProvider` interface and a default implementation (`ra-i18n-polyglot`) based on `node-polyglot`. This will be used as the foundation.
  - **`i18next` (Potentially):** For advanced features beyond `ra-i18n-polyglot` (e.g., more complex pluralization rules, context, namespace management for very large applications, or if the backend API provides translations in an i18next-compatible format), `i18next` along with `react-i18next` and an adapter for React-Admin (`ra-i18n-i18next`) COULD be considered. However, for the initial setup, React-Admin's default solution is targeted to keep complexity low.
  - Configuration will be in `src/providers/i18nProvider.ts`.

- **Translation File Structure & Format:**

  - **Format:** JSON files. Each file contains key-value pairs for one language.
  - **Structure:** One file per language containing all translations for the Control Plane UI. These files will be placed in the `controlplane-ui/public/locales/` directory (e.g., `controlplane-ui/public/locales/en.json`, `controlplane-ui/public/locales/de.json`).

        ```json
        // Example: public/locales/en.json
        {
          "ra": { // React-Admin internal translations
            "action": {
              "save": "Save",
              "cancel": "Cancel"
            },
            // ... other ra keys
          },
          "app": { // Application-specific translations
            "dashboard": {
              "title": "Dashboard",
              "welcomeMessage": "Welcome to the EAF Control Plane!"
            },
            "tenants": {
              "title": "Tenants",
              "fields": {
                "name": "Name",
                "identifier": "Identifier"
              },
              "actions": {
                "create": "Create Tenant"
              }
            },
            "validation": {
                "required": "This field is required."
            }
            // ... other app keys
          }
        }
        ```

  - Language files will be loaded by the `i18nProvider` at application startup.

- **Translation Key Naming Convention:**

  - **React-Admin Keys:** React-Admin uses its own keys for standard UI elements (e.g., `ra.action.save`). These can be overridden if necessary.
  - **Custom Application Keys:** For application-specific texts, a hierarchical structure starting with `app.` followed by feature/module, then component/context, and finally the specific element will be used.
    - Example: `app.tenants.fields.name` (for the "Name" field in tenant management).
    - Example: `app.notifications.tenantCreatedSuccess` (for a success message).
  - Keys MUST be consistent and well-documented (e.g., in a central document or directly in the base language files with comments).
  - Dynamically generated keys are to be avoided as they complicate static analysis and key extraction.

- **Process for Adding New Translatable Strings:**

  - Developers or AI agents MUST first add new translation keys to the primary development language file (e.g., `en.json`).
  - In the code, React-Admin's translation functions (e.g., the `useTranslate` hook or the `translate` HOC) MUST be used to render texts.

        ```tsx
        import { useTranslate } from 'react-admin';

        const MyComponent = () => {
          const translate = useTranslate();
          return (
            <div>
              <h1>{translate('app.dashboard.title')}</h1>
              <p>{translate('app.tenants.fields.name')}</p>
            </div>
          );
        };
        ```

  - For texts with dynamic values (variables) or HTML content, the i18n provider's corresponding functions are to be used (e.g., `translate('app.welcomeMessage', { name: userName })`).

- **Handling Pluralization:**

  - `ra-i18n-polyglot` supports simple pluralization rules via Polyglot's "smart count" functionality (e.g., `key_one: '1 item', key_other: '%{smart_count} items'`).
  - For more complex pluralization requirements beyond the basic functionality, the suitability of `i18next` (which supports ICU message format) would need to be evaluated. Initially, `ra-i18n-polyglot`'s capabilities will be used.

- **Date, Time, and Number Formatting:**

  - React-Admin components (like `<DateField>`, `<NumberField>`) handle localization of date, time, and number formats based on the active language and locale options passed to the `i18nProvider` or the components themselves.
  - For custom components displaying date, time, or number formats, JavaScript's native `Intl` API or a lightweight library like `date-fns` (with locale support) SHOULD be used to ensure correct localization. Formatting options should be kept consistent.

- **Default Language:** `en-US` (English, USA) will be defined as the fallback language. If a key is not found in the active language, it will fall back to English.

- **Language Switching Mechanism:**

  - React-Admin provides a `<LocalesMenuButton>` component that can be integrated into the `<AppBar>` to allow user language selection.
  - The selected language will be persisted in the application state (managed by React-Admin) and typically in `localStorage` to remember the user's choice for future visits.
  - The `i18nProvider` will be configured to detect the initial language from `localStorage` or the user's browser settings, defaulting to English if no preference is found.

- **Management of Translations by the Control Plane UI (Epic 6):**

  - The Control Plane UI itself will provide an interface for managing translation files for the EAF's *end applications*. The i18n strategy described here pertains to the multilingualism of the Control Plane UI *itself*. The functionality for managing external translation files (e.g., uploading, editing, downloading JSON files for other applications) will be implemented as a separate feature within the Control Plane UI (e.g., under `/i18n-administration` as hinted in the routing section) and will interact with the `eaf-controlplane-api` for storing and retrieving this data.

## Feature Flag Management

Feature flags (also known as feature toggles) allow for the gradual rollout of new functionalities to specific user groups, conducting A/B tests, or quickly disabling features if problems arise, without needing a new deployment.

- **Requirement Level:** For the ACCI EAF Control Plane UI, feature flags are initially classified as **"Used for specific rollouts and controlled releases."** This means not every small feature requires a flag, but they can be used for larger new modules, risky changes, or features intended for phased rollout. A comprehensive, deeply integrated feature flag culture is not mandatory initially, but the architecture should accommodate it.

- **Chosen Feature Flag System/Library:**

  - For initial needs, a **simple, configuration-based solution** is targeted. This could be via environment variables (at build time) or a configuration API provided by the backend (`eaf-controlplane-api`) queried at application startup.
  - **Option 1 (Environment Variables):** `VITE_FEATURE_NEW_DASHBOARD_ENABLED=true`. Simple for build-time flags, but not dynamically changeable at runtime without a new build/deployment.
  - **Option 2 (Backend Configuration Service):** The frontend application calls an endpoint of the `eaf-controlplane-api` at startup (e.g., `/controlplane/api/v1/ui-features`) that returns a JSON structure with active flags. This allows for more dynamic changes without frontend deployment.

        ```json
        // Example response from /ui-features
        {
          "newTenantDashboard": true,
          "betaUserImport": false,
          "experimentalLicenseReport": true
        }
        ```

  - **Preferred Solution:** **Option 2 (Backend Configuration Service)** is preferred as it offers more flexibility. The configuration of the flags themselves (which flags exist and their status) could become part of the Control Plane UI's administrative functions (meta-configuration).
  - External commercial systems (LaunchDarkly, Flagsmith, etc.) are not currently deemed necessary but can be evaluated in the future if complexity increases.

- **Accessing Flags in Code:**

  - A custom React Hook and/or a Context Provider will be created to encapsulate access to feature flags.
  - **Example (`src/hooks/useFeatureFlag.ts` and `src/store/contexts/FeatureFlagContext.tsx`):**

        ```typescript
        // src/store/contexts/FeatureFlagContext.tsx
        import React, { createContext, useContext, useEffect, useState, useMemo } from 'react';
        // Assuming apiClient is available for API calls
        // import { apiClient } from '../../providers/httpClient'; // Adjust path

        interface FeatureFlags {
          [key: string]: boolean;
        }

        interface FeatureFlagContextType {
          flags: FeatureFlags;
          isLoading: boolean;
          error: Error | null;
          isFeatureEnabled: (flagName: string) => boolean;
        }

        const FeatureFlagContext = createContext<FeatureFlagContextType | undefined>(undefined);

        export const FeatureFlagProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
          const [flags, setFlags] = useState<FeatureFlags>({});
          const [isLoading, setIsLoading] = useState(true);
          const [error, setError] = useState<Error | null>(null);

          useEffect(() => {
            const fetchFlags = async () => {
              setIsLoading(true);
              try {
                // const response = await apiClient.get<FeatureFlags>('/ui-features'); // Adjust path
                // setFlags(response.data);
                // Dummy data for the example:
                setFlags({
                  newTenantDashboard: true,
                  betaUserImport: false,
                });
                setError(null);
              } catch (err) {
                setError(err as Error);
                console.error('Failed to fetch feature flags', err);
                // Fallback to default flags or empty flags
                setFlags({});
              } finally {
                setIsLoading(false);
              }
            };
            fetchFlags();
          }, []);

          const isFeatureEnabled = (flagName: string): boolean => {
            return !!flags[flagName]; // Returns false if the flag doesn't exist or is false
          };
          
          const value = useMemo(() => ({ flags, isLoading, error, isFeatureEnabled }), [flags, isLoading, error]);

          return (
            <FeatureFlagContext.Provider value={value}>
              {children}
            </FeatureFlagContext.Provider>
          );
        };

        export const useFeatureFlag = (): ((flagName: string) => boolean) => {
          const context = useContext(FeatureFlagContext);
          if (context === undefined) {
            throw new Error('useFeatureFlag must be used within a FeatureFlagProvider');
          }
          return context.isFeatureEnabled;
        };

        export const useFeatureFlags = (): { flags: FeatureFlags; isLoading: boolean; error: Error | null } => {
          const context = useContext(FeatureFlagContext);
          if (context === undefined) {
            throw new Error('useFeatureFlags must be used within a FeatureFlagProvider');
          }
          return { flags: context.flags, isLoading: context.isLoading, error: context.error };
        };
        ```

        The `FeatureFlagProvider` would wrap the `App` component. In code, `const isNewDashboardEnabled = useFeatureFlag()('newTenantDashboard');` would be used.

- **Flag Naming Convention:**

  - Flags SHOULD be named concisely and clearly, typically in camelCase or PascalCase if used as keys in an object.
  - Examples: `newTenantDashboard`, `enableExperimentalReporting`, `useV2UserProfilePage`.
  - A prefix like `feature_` or `flag_` is optional but can aid clarity (e.g., `feature_newTenantDashboard`).

- **Code Structure for Flagged Features:**

  - **Conditional Rendering:** The most common use case is conditionally rendering components or UI elements:

        ```tsx
        const isNewDashboardEnabled = useFeatureFlag()('newTenantDashboard');
        // ...
        {isNewDashboardEnabled ? <NewDashboardComponent /> : <OldDashboardComponent />}
        ```

  - **Conditional Routes:** Entire routes can be registered conditionally.
  - **Conditional Logic:** Logic in functions or hooks can branch based on flags.
  - **Avoiding Complexity:** Care should be taken that feature flags do not lead to excessive branching and complexity in the code. Flags should be checked at the highest possible points in the component or logic hierarchy.

- **Strategy for Code Cleanup (Post-Flag Retirement):**

  - **Mandate:** Once a feature flag is fully rolled out (100% of the target audience) and considered permanent, or the feature is completely removed, the flag itself, all conditional logic, and all old/unused code paths MUST be removed from the codebase within {e.g., 2-3 sprints}.
  - This is an important aspect of avoiding technical debt. For every introduced flag, a ticket or task for later cleanup SHOULD be created.

- **Testing Flagged Features:**

  - **Manual Tests:** QA and developers MUST have the ability to test different flag configurations. This can be through local configuration overrides or a debug UI that allows toggling flags in the browser (if flags are client-side flexible).
  - **Automated Tests (E2E):** Playwright tests SHOULD be designed to run with different flag configurations to cover both code paths (feature enabled/disabled). This can be achieved by setting mocks for the feature flag service or by parameterizing test runs.

## Frontend Security Considerations

Frontend application security is crucial for protecting user data and preventing attacks. This section describes mandatory frontend-specific security practices that complement the main architecture document. AI agents and developers MUST follow these guidelines.

- **Cross-Site Scripting (XSS) Prevention:**

  - **Framework Reliance:** React, by default, renders data as text and not HTML, providing basic protection against XSS when JSX is used correctly. Direct DOM manipulation (e.g., via `dangerouslySetInnerHTML`) MUST be avoided. If absolutely unavoidable, the content MUST be explicitly sanitized beforehand, either server-side or client-side with an established library like DOMPurify (configured for strict filtering).
  - **React-Admin:** React-Admin and underlying UI libraries (like Material UI) are designed to minimize XSS risks when used as intended.
  - **Content Security Policy (CSP):** A strict Content Security Policy MUST be implemented server-side (via HTTP headers) as specified in the main architecture document. The frontend must ensure it is compliant with this CSP (e.g., no inline scripts or styles without a nonce/hash, restriction of `script-src`, `style-src`, `connect-src` to trusted sources).

- **Cross-Site Request Forgery (CSRF) Protection:**

  - **Mechanism:** Since authentication is via tokens (JWTs) in the `Authorization` header and not via session-based cookies for API requests, the traditional CSRF risk for API calls is reduced.
  - Nevertheless, all state-changing requests (POST, PUT, DELETE) MUST ensure they cannot be triggered by cross-site scripting on other websites initiating such requests to the EAF API. The `Authorization` header approach is already a strong protection here.
  - The backend (per main architecture document) SHOULD additionally implement mechanisms like SameSite cookie attributes (if cookies are used for other purposes) and possibly check the `Origin` or `Referer` header for critical operations.

- **Secure Token Storage & Handling (JWTs):**

  - **Storage Mechanism:** JWTs (Access Tokens) MUST be stored securely in the frontend.
    - **Preferred Method:** Storage in JavaScript application memory (e.g., in a React Context or Zustand store). Tokens are then only valid for the duration of the browser session (tab/window) and not vulnerable to XSS attacks targeting `localStorage` or `sessionStorage`.
    - `HttpOnly` Cookies for Refresh Tokens: If a refresh token mechanism is used, the refresh token SHOULD be stored in an `HttpOnly`, `Secure`, `SameSite=Strict` (or `Lax`) cookie set by the backend. The frontend does not access it directly. The access token is then held in memory.
    - `localStorage` and `sessionStorage` MUST NOT be used for storing JWTs as they are vulnerable to XSS attacks.
  - **Token Refresh:** The `authProvider` and `httpClient` (as described in the API Interaction Layer) MUST securely handle the logic for automatically refreshing access tokens using refresh tokens (if implemented).

- **Third-Party Script Security:**

  - **Policy:** Inclusion of third-party scripts (e.g., for analytics, monitoring, if needed) MUST be limited to an absolute minimum, and each source carefully vetted.
  - **Subresource Integrity (SRI):** For all external scripts and stylesheets loaded from CDNs, Subresource Integrity (SRI) hashes MUST be used if available. This ensures that the loaded files have not been tampered with.

        ```html
        <script src="https{://example.com/library.js}"
                integrity="sha384-oqVuAfXRKap7fdgcCY5uykM6+R9GqQ8K/uxy9rx7HNQlGYl1kPzQho1wx4JwY8wC"
                crossorigin="anonymous"></script>
        ```

  - Third-party scripts SHOULD, where possible, be loaded asynchronously (`async`/`defer`) to minimize blocking of the main thread.

- **Client-Side Data Validation:**

  - **Purpose:** Client-side validation primarily serves to improve the user experience through immediate feedback. It is NOT a substitute for server-side validation.
  - **Mandate:** All critical data validations MUST occur server-side in the `eaf-controlplane-api` (as defined in the main architecture document).
  - **Implementation:** React-Admin forms provide validation props. These SHOULD be used to perform basic checks (e.g., required fields, format checks) client-side. Validation rules should, where appropriate, mirror server-side rules.

- **Preventing Clickjacking:**

  - **Mechanism:** The primary defense against clickjacking is the `X-Frame-Options` HTTP header (e.g., `DENY` or `SAMEORIGIN`) or the `frame-ancestors` directive in the Content Security Policy. These headers MUST be set server-side (per main architecture document). The frontend should not rely on frame-busting scripts.

- **API Key Exposure (for client-side consumed services):**

  - **Restriction:** API keys used exclusively client-side (e.g., for a map service, if relevant) MUST be restricted as much as possible via the service provider's console (e.g., through HTTP referrer restrictions, IP address filters, or API-specific access restrictions).
  - **Backend Proxy:** For API keys requiring more secrecy or controlling sensitive operations, a backend proxy endpoint MUST be created in the `eaf-controlplane-api`. The frontend calls the proxy, not the third-party service directly.

- **Secure Communication (HTTPS):**

  - **Mandate:** All communication between the frontend and the `eaf-controlplane-api`, as well as all other external services, MUST use HTTPS. Mixed content (HTTP assets on an HTTPS page) is forbidden and will be prevented by the CSP.

- **Dependency Vulnerabilities:**

  - **Process:** `npm audit` (or `yarn audit`) MUST be run regularly and as part of the CI pipeline to identify known vulnerabilities in project dependencies.
  - Vulnerabilities with high or critical severity MUST be addressed before deployment, either by updating the dependency or by applying recommended workarounds.
  - Tools like Dependabot (GitHub) or Snyk MAY be used for automatic monitoring and notification of new vulnerabilities.

- **Security Headers:**

  - In addition to CSP and `X-Frame-Options`, other security-related HTTP headers SHOULD be configured server-side, such as `Strict-Transport-Security` (HSTS), `X-Content-Type-Options`, `Referrer-Policy`. The frontend architecture must be compatible with these headers.

## Browser Support and Progressive Enhancement

This section defines the target browsers for the ACCI EAF Control Plane UI and describes how the application should behave in less capable or non-standard environments.

- **Target Browsers:**

  - The ACCI EAF Control Plane UI MUST be fully functional and correctly displayed on the **latest two stable versions** of the following desktop browsers:
    - Google Chrome
    - Mozilla Firefox
    - Microsoft Edge
    - Apple Safari (macOS)
  - **Internet Explorer (IE) in any version is NOT supported.**
  - Although the PRD specifies a "desktop-first" orientation, it is desirable for the application to also be usable on modern tablet browsers (current versions of Safari on iPadOS and Chrome on Android). Full optimization for mobile devices is not the primary focus initially, but basic responsiveness for smaller viewports (such as those created by resizing desktop windows) should be provided.

- **Polyfill Strategy:**

  - **Mechanism:** Vite in conjunction with Babel (typically via `@vitejs/plugin-react`, which uses Babel) and PostCSS (for CSS prefixes) will be used to ensure compatibility with target browsers.
    - `@babel/preset-env` (or a similar configuration in Vite) will be configured to target the browser support matrix defined above and provide necessary JavaScript polyfills for ECMAScript features that may not yet be fully supported by these browsers. This is often done through automatic injection of polyfills from `core-js`.
  - **Specific Polyfills:** Beyond the automatic polyfills from `core-js`, no specific additional polyfills are currently expected. If, during development, features are implemented that rely on very new browser APIs not covered by `core-js` and missing in target browsers, the need for specific polyfills (e.g., for `IntersectionObserver` if not universally available, or specific `Intl` features) will be evaluated and documented on a case-by-case basis.

- **JavaScript Requirement & Progressive Enhancement:**

  - **Baseline:** The core functionality of the ACCI EAF Control Plane UI **REQUIRES JavaScript enabled** in the browser. It is a Single Page Application (SPA) that relies heavily on JavaScript for rendering, logic, and API interactions.
  - **No-JS Experience:** No specific No-JS fallback view or functionality will be provided, other than a simple message (e.g., via a `<noscript>` tag in `index.html`) advising the user to enable JavaScript.

        ```html
        <noscript>
          This application requires JavaScript to function correctly. Please enable JavaScript in your browser settings.
        </noscript>
        ```

  - Although Progressive Enhancement is a desirable principle, the focus for this complex administrative application is on a rich, interactive user experience that presupposes JavaScript.

- **CSS Compatibility & Fallbacks:**

  - **Tooling:** PostCSS with `autoprefixer` MUST be used (typically configured by default in Vite projects) to automatically add vendor prefixes for CSS properties that still require them for the target browser matrix. The browser list for Autoprefixer will be derived from the "Target Browsers."
  - **Feature Usage:**
    - Modern CSS features (e.g., Flexbox, Grid, Custom Properties) can and should be used, as they are well-supported by target browsers.
    - CSS features not supported in part of the target browser matrix MAY only be used if an acceptable "Graceful Degradation" behavior (i.e., the page remains usable and understandable even if the feature does not apply) is ensured, or if an explicit fallback (e.g., via `@supports` queries) is implemented. The use of such features should be coordinated within the team.

- **Accessibility Fallbacks:**

  - As described in the "Accessibility (AX) Implementation Details" section, the use of ARIA attributes is important. It is assumed that target browsers and assistive technology used by users support modern ARIA versions. For very old assistive technologies that may not support all ARIA 1.1/1.2 features, the basic semantic HTML will continue to provide baseline accessibility.

## Change Log

This change log documents significant changes and developments in this Frontend Architecture Document. It serves to track decisions and progress.

| Change                                     | Date        | Version | Description                                                                                                | Author           |
| :----------------------------------------- | :---------- | :------ | :--------------------------------------------------------------------------------------------------------- | :--------------- |
| Initial Draft of Frontend Architecture Doc | May 16, 2025| 0.1.0   | First complete draft of the Frontend Architecture Document based on the main architecture document, PRD, and initial prompt. | Design Architect |
| {Further changes to be entered here}       | {Date}      | {Vers.} | {Description of change}                                                                              | {Author}         |
