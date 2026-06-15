# Feature Specification: GluFree Core Features (MVP)

**Feature Branch**: `001-glufree-core`

**Created**: 2025-01-16

**Status**: Draft

**Input**: Complete core feature requirements for GluFreeListApp: Catalog, Search/Filters, Product Detail, Offline Favorites, and Visual Accessibility

---

## Vision & Product Overview

GluFreeListApp is a multiplatform discovery and management application for gluten-free products. The core MVP provides users with an intuitive interface to explore a curated catalog of gluten-free products, search and filter by category, view detailed product information with ingredient lists and certifications, and maintain a local favorites list accessible offline. The application prioritizes performance (mobile-first), accessibility (WCAG 2.1 AA compliance), and data integrity (food safety-critical information).

### Target Platforms
- **Android**: API 34+ (minimum)
- **iOS**: iOS 17+ (minimum)  
- **Web**: Kotlin/Wasm (modern browsers, Chrome/Safari/Firefox latest 2 versions)

### Core Value Proposition
Empower users with quick, offline-accessible discovery of verified gluten-free products while shopping or researching, with confidence in data accuracy and zero dependency on internet connectivity for saved products.

---

## User Personas & Journeys

### Persona 1: Maria - Newly Diagnosed Celiac (Age 28, Urban)
**Goals**: Find safe gluten-free products quickly while shopping; understand ingredient transparency
**Pain Points**: Uncertain about cross-contamination; overwhelmed by product choices
**Journey**: Browse catalog → Search for specific category → Review ingredients + certifications → Save favorites for offline reference

### Persona 2: Alex - Experienced Gluten-Free Parent (Age 35, Primary Caregiver)  
**Goals**: Quickly verify product safety; build a personal library of trusted products for family dietary needs
**Pain Points**: App crashes lose product history; slow search when connection unstable at grocery store
**Journey**: Search favorite brands → Filter by "kid-friendly" subcategories → Pin favorites → Access offline when shopping without internet

### Persona 3: Jordan - Health-Conscious Adventurer (Age 42, Remote Worker)
**Goals**: Discover local gluten-free options while traveling; customize search by region/availability
**Pain Points**: Product information quality inconsistent; apps require constant clicks to understand nutrition
**Journey**: Browse regional catalog → Save top candidates → Review certification details → Keep offline backup for reference

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Infinite-Scrolling Product Catalog with Async Image Loading (Priority: P1)

Users need to browse a responsive grid of gluten-free products without waiting for the entire catalog to load. Images must load asynchronously without blocking the UI, allowing users to scroll smoothly through hundreds of products while exploring.

**Why this priority**: Core MVP functionality - users cannot discover products without a functional catalog. Async image loading prevents "frozen" UI on slow networks (critical for field usage).

**Independent Test**: This feature is testable in isolation. A user should be able to: 
1. Launch the app → See a grid of 12-20 initial products
2. Scroll to bottom → Trigger automatic pagination load
3. Continue scrolling → Load 3+ additional pages without UI lag
4. Verify images load progressively without blocking catalog navigation

**Acceptance Scenarios**:

1. **Given** app is launched, **When** user views the Catalog tab, **Then** a responsive grid displays products (2 columns on phones, 3+ on tablets) with images, name, category badge, and gluten-free certification indicator

2. **Given** user scrolls to the bottom of visible products, **When** 80% of the current page is in viewport, **Then** system automatically loads the next page of 20 products without user interaction

3. **Given** images are loading from slow network (simulated 3G), **When** user scrolls past products, **Then** images load progressively in background without blocking scroll performance; placeholder/skeleton shows until image ready

4. **Given** user navigates away from catalog (to product detail), **When** user returns to catalog, **Then** scroll position is restored to previous location within 100px accuracy

5. **Given** pagination reaches end of catalog, **When** user scrolls to bottom, **Then** "No more products" indicator displays; no infinite loop attempts

6. **Given** network fails mid-pagination, **When** pagination load fails, **Then** error state shown with "Retry" button; existing loaded products remain visible

### Edge Case - Catalog Pagination:
- What happens when user scrolls faster than pagination can load? → Pagination queue is smart; multiple rapid scrolls are collapsed into single request
- How does system handle corrupted image data? → Fallback to product icon/placeholder; image error doesn't crash app
- What if product count changes during session? → Pagination tokens are server-validated; deduplication on client

---

### User Story 2 - Real-Time Search with Debounce & Category Chips (Priority: P1)

Users need to find specific products quickly using a search bar with 500ms debounce and interactive category filter chips. Search results must be responsive even on slow networks, with a persistent session history of recent searches.

**Why this priority**: Core discovery mechanism - without search, users navigate blindly through pagination. 500ms debounce balances responsiveness with network efficiency.

**Independent Test**: 
1. Focus on search field → Type "almond" → See results appear after 500ms debounce
2. Tap category chip "Snacks" → Results filter instantly; debounce timer resets
3. Clear search → Tap another chip "Desayuno" → See filtered results without retyping
4. Close app → Reopen → See last 5 searches in search history dropdown

**Acceptance Scenarios**:

1. **Given** user taps search field, **When** search field is focused, **Then** search history dropdown appears showing up to 5 recent searches with clear buttons

2. **Given** user types a search query letter-by-letter, **When** >500ms passes since last keystroke, **Then** search API is called once; partial results don't trigger multiple requests (debounce verified)

3. **Given** search results are displayed, **When** user taps a category chip (e.g., "Snacks"), **Then** results filter immediately; search box retains text; results now show only matching category

4. **Given** multiple category chips are available, **When** user taps second chip while first is active, **Then** filters AND together (show products matching BOTH categories); visual indicator shows active chips

5. **Given** search has results, **When** user clears search text, **Then** results revert to full catalog view; category chip filters still active

6. **Given** user performs search query, **When** search completes and user closes search field, **Then** query is saved to session history (persisted to local storage)

7. **Given** search history has 5+ entries, **When** user performs new search, **Then** oldest history entry is removed; list stays max 5 entries

8. **Given** search returns 0 results, **When** no products match query, **Then** friendly "No products found" message displays with suggestion to adjust filters or try new search

### Edge Case - Search & Filters:
- What if user has spotty connection and types very fast? → Intermediate requests are cancelled; only final debounced search is sent
- How are special characters handled? (e.g., "café", "Müller brand") → UTF-8 normalization applied; search is case-insensitive
- Can user combine search text + multiple category chips? → Yes; search AND filters are applied together

---

### User Story 3 - Product Detail View with Ingredients & Certifications (Priority: P1)

Users need to examine detailed product information including complete ingredient lists, allergen warnings, certification status (certified gluten-free vs. verified), category, and a clear call-to-action to save to favorites. This is critical for food safety decisions.

**Why this priority**: Data integrity and user safety are constitutional requirements (Principle I). Users make health decisions based on this information; incomplete or incorrect detail view creates liability.

**Independent Test**:
1. Tap product in catalog → Detail view opens
2. See ingredient list with highlightable/scannable format
3. Verify gluten-free certification badge is visible
4. Tap "Add to Favorites" → Clear confirmation
5. Verify product is searchable in Favorites tab

**Acceptance Scenarios**:

1. **Given** user taps any product in catalog, **When** product detail view opens, **Then** it displays: product image (large), product name, category, certification badge (certified/verified/uncertain), price/brand info, full ingredient list

2. **Given** product has ingredient list, **When** detail view loads, **Then** ingredients are listed in clear, readable format (one per line, or visually grouped); allergen information (e.g., "May contain nuts") is highlighted in distinct color

3. **Given** product has gluten-free certification, **When** detail view is displayed, **Then** prominent badge indicates certification authority (e.g., "GFCO", "CeliacUK", "Verified Gluten-Free"); uncertain sources show "Verified by community" label

4. **Given** user scrolls in detail view, **When** call-to-action button comes into view, **Then** "Add to Favorites" button is sticky at bottom or clearly visible; button is minimum 48x48dp touch target (WCAG 2.1 AA)

5. **Given** product is in user's favorites, **When** detail view is shown for that product, **Then** button state changes to "Remove from Favorites" with visual distinction (e.g., filled icon)

6. **Given** user taps "Add to Favorites", **When** action completes, **Then** success toast/confirmation appears ("Added to Favorites"); product syncs to local storage immediately

7. **Given** user is offline, **When** viewing detail of previously cached product, **Then** full information displays; "Added offline" indicator shows if action taken without sync

8. **Given** detail view has long ingredient list, **When** user scrolls, **Then** image stays visible in a small header element; section headers (Ingredients, Certifications) are sticky to aid navigation

### Edge Case - Product Details:
- What if ingredient data is incomplete? → "Ingredient list not available" shows; user advised to check product packaging
- How are multiple certifications shown? → List all certifications with distinct badges/icons
- What if user's edition of product varies by region? → Show region-specific variant info; note "This listing is for [country] version"

---

### User Story 4 - Offline-First Favorites with Local Persistence & Sync (Priority: P1)

Users need to save favorite products to device storage and access them completely offline, with automatic synchronization to a cloud account when internet connectivity returns. This is essential for shopping scenarios where connectivity is unstable.

**Why this priority**: Non-functional requirement tied to Performance-First Design (Principle III). Users shop in areas with poor signal; app must function offline. MVP success depends on reliability in real-world field use.

**Independent Test**:
1. Add 5 products to Favorites (online)
2. Turn off internet → App still functional
3. Tap Favorites tab → See all 5 products, swipe to remove one
4. Turn internet back on → Favorites automatically sync to server
5. Clear app data → Restore favorites from cloud

**Acceptance Scenarios**:

1. **Given** user is online and adds products to favorites, **When** product is saved, **Then** it is immediately stored in local device storage (SQLDelight DB) and optionally synced to cloud if account exists

2. **Given** user has internet off (verified by connectivity check), **When** user navigates to Favorites tab, **Then** favorites list displays all locally saved products; UI confirms offline status ("You're offline - viewing local favorites")

3. **Given** user is offline and deletes a favorite, **When** deletion is confirmed, **Then** product is removed from local storage immediately; deletion is queued for sync when online

4. **Given** network connectivity returns, **When** app detects connection, **Then** sync process runs automatically in background: upload local changes, download any cloud updates, resolve conflicts by "last-write-wins" strategy

5. **Given** user's favorites list is empty, **When** Favorites tab is viewed, **Then** friendly message appears ("No favorites yet - browse products to add them") with CTA to return to catalog

6. **Given** favorites count exceeds reasonable UI threshold (e.g., 1000), **When** user manually scrolls or searches within favorites, **Then** pagination applies to favorites (same as catalog) to maintain performance

7. **Given** user has synced favorites to cloud account, **When** user uninstalls app and reinstalls on same device/account, **Then** favorites are restored from cloud automatically on first login

8. **Given** sync conflict occurs (e.g., user deleted locally, but server has new version), **When** sync resolves conflict, **Then** local action wins; conflict logged for potential manual review

### Edge Case - Offline Favorites:
- What if local storage is full? → Graceful degradation: oldest items are moved to archive; user notified
- How does sync handle deleted account? → Favorites remain local; sync is permanently disabled
- What if user has 50K+ favorites across accounts? → Improbable scenario; sync is paginated (100 items/request)

---

### User Story 5 - Light/Dark Mode & WCAG 2.1 AA Accessible Design (Priority: P2)

Users need automatic light/dark mode theming based on device settings, with consistent typography, readable color contrasts (WCAG 2.1 AA minimum), and responsive layouts for all screen sizes. Accessibility is a constitutional requirement.

**Why this priority**: P2 because catalog/search/detail/favorites work without this, but accessibility is mandatory per Principle VII. Visual design must not inhibit usage by visually impaired users or those with color blindness.

**Independent Test**:
1. Launch app on Android with Dark Mode enabled → theme automatically applies
2. Change device theme to Light → app updates theme without restart
3. Run accessibility scanner (Accessibility Scanner Android, Accessibility Inspector iOS) → 0 critical issues
4. Verify text contrast ratios: heading 7:1, body 4.5:1 minimum against backgrounds
5. Rotate device to landscape → layouts adapt; no content hidden or overlapped

**Acceptance Scenarios**:

1. **Given** device has Dark Mode enabled (system-level), **When** app launches, **Then** dark theme is applied automatically without user configuration; all UI elements follow dark palette

2. **Given** user changes device theme (light ↔ dark), **When** app is active, **Then** theme updates immediately (without restart); animation smooths transition

3. **Given** text elements are displayed, **When** contrast ratio is measured, **Then** normal text meets 4.5:1 contrast (WCAG AA); large text (18pt+) meets 3:1 contrast

4. **Given** heading and button text is shown, **When** contrast is measured, **Then** contrast ratio is 7:1 or higher (WCAG AAA preferred for critical elements like certifications)

5. **Given** color is used to encode information (e.g., red badge = "contains gluten"), **When** information is conveyed, **Then** color is supplemented with icon, text label, or other non-color cue

6. **Given** user has screen reader enabled (TalkBack on Android, VoiceOver on iOS), **When** navigating catalog, **Then** all elements are announced: product name, category, certification status, price

7. **Given** screen reader is active, **When** "Add to Favorites" button is focused, **Then** button state is announced ("Add to Favorites, button" or "Remove from Favorites, button" when active)

8. **Given** device is in landscape orientation, **When** app layout reflows, **Then** catalog grid adjusts to 4+ columns (tablets); detail view and favorites remain usable without horizontal scroll

9. **Given** user has large text setting enabled (Android: "Large" font, iOS: Accessibility > Larger Accessibility Sizes), **When** app displays, **Then** text scales proportionally; layout remains functional (no overlap, essential content visible)

10. **Given** user is color-blind (simulated via accessibility settings), **When** viewing products with certification badges, **Then** certification status conveyed via icon + text, not color alone

### Edge Case - Theming & Accessibility:
- What if device has custom color profile (high saturation)? → App respects system color adjustments; fallback to standard palette if distorted
- How are animated elements handled for users with motion sensitivity? → Motion toggle respected; animations disabled if "Reduce Motion" is enabled
- What if font size is set to maximum? → Layout uses flexible heights; critical UI shifts to avoid cutoff

---

### Edge Cases (Cross-Feature)

- **Network Disconnection During Pagination**: User scrolls catalog, pagination request starts, network drops mid-request → existing products remain visible; error state shown; retry option available
- **Product Data Updates During Browsing**: Server adds/removes products during user's session → pagination tokens remain valid; user doesn't see duplicates; deleted products handled gracefully
- **Corrupted Local Storage**: User's SQLDelight database is corrupted → app detects on launch; graceful reset of local data with option to sync from cloud
- **API Rate Limiting**: User searches too frequently or catalog receives traffic spike → rate limit errors handled gracefully; backoff strategy applied; user informed ("Takes a moment...")
- **Very Large Ingredient Lists**: Product has 100+ ingredients → scrollable list with sticky section headers; search within ingredient list for specific allergen
- **Missing Product Images**: Image URL is broken or returns 404 → placeholder icon/color swatch shows; product remain discoverable

---

## Requirements *(mandatory)*

### Functional Requirements

#### Catalog Feature
- **FR-001**: System MUST display a responsive grid of gluten-free products (initial page load: 12-20 items)
- **FR-002**: Catalog grid MUST support infinite pagination; automatic load triggered when user scrolls to 80% of current page
- **FR-003**: Images MUST load asynchronously and must not block UI scrolling or navigation
- **FR-004**: System MUST restore user's scroll position when navigating back to catalog (within 100px tolerance)
- **FR-005**: Pagination MUST include deduplication logic; duplicate products across pages must not appear
- **FR-006**: System MUST display pagination error states with clear retry mechanism

#### Search & Filtering Feature
- **FR-007**: System MUST provide a search input with 500ms debounce; only one search request per debounce window
- **FR-008**: Search MUST apply case-insensitive, UTF-8 normalized matching across product names and descriptions
- **FR-009**: System MUST display up to 5 most recent searches in dropdown history; history persisted per session
- **FR-010**: Category filter chips MUST apply instantly (no debounce); multiple chips operate as AND filter
- **FR-011**: Search filters MUST persist during user's session; cleared only on explicit user action or app restart
- **FR-012**: System MUST display "No results" message with helpful guidance when search returns zero matches

#### Product Detail Feature
- **FR-013**: Product detail view MUST display: product image (large), name, brand, category, full ingredient list, certification badges, and price/availability
- **FR-014**: Ingredient lists MUST be formatted for readability (one per line or grouped visually); allergen information highlighted
- **FR-015**: Certification badge MUST indicate authority (GFCO, CeliacUK, verified by community, uncertain) with visual distinction
- **FR-016**: System MUST preserve ingredient and certification data sourcing/verification date (displayed as "Last verified: [date]")
- **FR-017**: "Add/Remove Favorites" button MUST be minimum 48x48dp (iOS: 44x44pt) touch target; always visible or sticky
- **FR-018**: System MUST show clear confirmation/state change when product is added to or removed from favorites

#### Offline Favorites Feature
- **FR-019**: System MUST persist user's favorite products to local device storage (SQLDelight) immediately when added
- **FR-020**: Favorites MUST be accessible and fully functional when device has no network connectivity
- **FR-021**: Favorites tab MUST display count of saved products and pagination if count exceeds 100
 - **FR-022**: System MUST automatically sync favorites to cloud when connectivity returns (if user has account)
 - **FR-023**: Sync conflicts (e.g., local delete vs. server update) MUST resolve by a merge-first strategy: union of favorites with de-duplication; deletions are handled via tombstone timestamps (later deletion timestamp wins for same product)
 - **FR-024**: System MUST handle restoration of favorites from cloud account after app reinstall

Note: On first login, the app SHOULD present the user with an explicit merge dialog offering: (1) Merge local favorites into account (recommended), (2) Keep local only, or (3) Replace local with server favorites. This prevents silent data loss and respects user intent.

#### Visual Accessibility & Theming
- **FR-025**: System MUST automatically apply light/dark theme based on device system settings (Android: Dark Mode, iOS: Appearance)
- **FR-026**: Visual theme MUST include consistent typography: heading hierarchy, body text, captions across all platforms
- **FR-027**: All text colors MUST meet WCAG 2.1 AA contrast ratio (4.5:1 for normal text, 3:1 for large text)
- **FR-028**: Buttons and interactive elements MUST maintain 7:1 contrast ratio (WCAG AAA preferred)
- **FR-029**: Color MUST NOT be the only means of conveying information (e.g., certification status also indicated via icon/text)
- **FR-030**: Screen reader support MUST allow navigation of all features; all actionable elements must be announced
- **FR-031**: Responsive layout MUST adapt to portrait/landscape and support device text scaling (1.0x to 2.0x)
- **FR-032**: Motion-sensitive users (Reduce Motion enabled) MUST not experience problematic animations

### Data Model & Entities

#### Product Entity
- **id**: Unique product identifier (UUID or server-assigned)
- **name**: Product display name (string, max 200 chars)
- **description**: Short product description (string, max 500 chars, optional)
- **brand**: Product manufacturer/brand (string, 100 chars)
- **category**: Primary product category (enum: Snacks, Breakfast, Dairy, Beverages, Prepared Foods, Baking, Other)
- **subcategory**: Secondary category (string, optional, e.g., "Protein Bars", "Granola")
- **ingredients**: Full ingredients list (array of strings, one per ingredient)
- **allergens**: Known allergens present (array of strings: gluten, nuts, dairy, etc.)
- **certifications**: Array of certification objects:
  - **type**: (enum: GFCO, CeliacUK, NSF, Community Verified, Uncertain)
  - **authority**: Name of issuing authority
  - **verificationDate**: ISO date when certification was verified
- **imageUrl**: CDN URL to product image
- **price**: Product price (decimal, optional - fetched from external source)
- **sourceUrl**: URL to original product listing (e.g., OpenFoodFacts)
- **dataSource**: Which database this product came from (enum: OpenFoodFacts, UserContribution, CertificationDB)
- **lastUpdated**: Timestamp when product data was last verified/updated (ISO 8601)
- **isCertified**: Boolean - true if gluten-free certified by recognized authority
- **createdAt**: Timestamp product was added to system
- **updatedAt**: Timestamp of last modification
 - **externalIdentifiers**: Array of external identifier objects (e.g., barcode/GTIN, OpenFoodFacts id) with structure { "type": "barcode"|"openfoodfacts_id"|"other", "value": "string" }
 - **canonicalBarcode**: Preferred barcode/GTIN string when available (nullable)
 - **previousIdentifiers**: Array of historical identifiers retained for redirect/merge purposes
 - **versionedAt**: Timestamp for versioning/audit when this product snapshot was produced

#### UserFavorite Entity (Local to Client)
- **id**: Local unique identifier (UUID)
- **userId**: (Optional, if user authenticated) User account ID
- **productId**: Foreign key to Product
- **addedAt**: Timestamp when added to favorites (ISO 8601)
- **syncStatus**: Enum (synced, pending_upload, pending_delete, conflict)
- **lastSyncAt**: Timestamp of last sync attempt (optional)

#### SearchHistory Entity (Client-Local)
- **id**: Local unique identifier
- **query**: Search text (string, max 200 chars)
- **timestamp**: When search was performed (ISO 8601)
- **resultCount**: How many results returned (for analytics)
- **isFromChip**: Boolean - true if filter chip was applied

#### Theme/Preference Entity (Client-Local)
- **systemTheme**: Current system theme (light/dark) - read-only from device
- **userThemeOverride**: Optional user override (light/dark/auto) - nullable
- **accessibilitySettings**: Object containing:
  - **screenReaderEnabled**: Boolean
  - **reduceMotion**: Boolean
  - **largeText**: Boolean
  - **highContrast**: Boolean

### API Contracts & Endpoints

#### GET /products
Fetch paginated catalog with optional filters
- **Query Parameters**:
  - `page` (int, default 1): Page number
  - `limit` (int, default 20, max 100): Items per page
  - `category` (string, optional): Filter by category name
  - `search` (string, optional): Search query
  - `certified_only` (boolean, default false): Show only GFCO/CeliacUK certified
- **Response**:
  ```json
  {
    "data": [{ product objects }],
    "pagination": {
      "currentPage": 1,
      "totalPages": 50,
      "totalCount": 1000,
      "hasNext": true
    }
  }
  ```

#### GET /products/:id
Fetch single product detail
- **Response**: Full Product object with all fields, including certifications array

#### POST /users/:userId/favorites
Add product to user's favorites
- **Body**: `{ "productId": "uuid" }`
- **Response**: Added UserFavorite object

#### DELETE /users/:userId/favorites/:favoriteId
Remove product from favorites
- **Response**: HTTP 204 No Content

#### GET /users/:userId/favorites
Fetch user's synchronized favorites (paginated)
- **Query Parameters**: `page`, `limit` (same as /products)
- **Response**: Array of UserFavorite with nested Product objects

#### POST /sync/favorites
Bulk sync favorites (for offline-first sync)
- **Body**:
  ```json
  {
    "additions": [{ "productId": "uuid", "addedAt": "2025-01-16T10:00:00Z" }],
    "deletions": [{ "productId": "uuid", "deletedAt": "2025-01-16T10:00:00Z" }],
    "lastSyncAt": "2025-01-15T10:00:00Z",
    "conflictResolution": "merge_union"
  }
  ```
 - **Response**:
  ```json
  {
    "synced": { "additions": [...], "deletions": [...] },
    "serverFavorites": [{ /* UserFavorite objects; conflicts resolved and conflict metadata included */ }],
    "timestamp": "2025-01-16T10:05:00Z"
  }
  ```

---

## Non-Functional Requirements

### Performance

- **Catalog Load Time**: Initial catalog page must render in < 1.5 seconds (p95) on mid-range Android device (e.g., Snapdragon 778G)
- **Image Loading**: Images must progressively load; placeholder shown within 500ms of scroll into view
- **Search Debounce**: Search API request only fired once per 500ms minimum; no request spam
- **Pagination Load**: Next page fetch must complete within 2 seconds (p95); existing products remain interactive
- **Memory Usage**: App must not exceed 150MB resident memory on Android; < 100MB on iOS
- **Battery Impact**: 1 hour of app usage (scrolling, searching, viewing) must consume < 15% battery on typical device
- **Offline Sync**: Sync of up to 500 favorites must complete within 10 seconds on LTE network

### Security & Data Safety

As per Constitution Principle I (Data Integrity & Safety-First):
- **Food Safety Data**: All gluten markers and cross-contamination risk data must have documented sources; sources displayed in UI
- **Encryption**: User data in transit covered by TLS 1.3+; at-rest encryption for preferential data (optional for MVP)
- **API Authentication**: JWT tokens with 30-day expiration; refresh tokens rotated on use
- **No Credentials on Client**: No API keys or secrets stored in app code; all sensitive auth via secure backend
 - **Authentication Strategy (MVP)**: Email/password with email verification + JWT access/refresh tokens (refresh rotation). MFA: optional (not required for MVP).

### Accessibility (WCAG 2.1 AA)

- **Color Contrast**: Minimum 4.5:1 for normal text; 3:1 for large text; heading text 7:1
- **Touch Targets**: Minimum 48x48dp (Android), 44x44pt (iOS)
- **Screen Reader**: All meaningful content and controls announced; skip links for navigation
- **Keyboard**: All features navigable via keyboard; tab order logical
- **Motion**: Animations respect device "Reduce Motion" setting

### Responsive Design

- **Phone Layouts**: Automatic layout for screens 320px–480px (portrait & landscape)
- **Tablet Layouts**: Grid expands to 3+columns on screens > 768px; detail view benefits from side-by-side layout options
- **Font Scaling**: Text respects device font size settings (1.0x to 2.0x without layout break)

### Offline Capability

- **Offline Duration**: App functions for minimum 7 days without network (assuming no new data synced)
- **Local Cache**: Product catalog limited to recently viewed/favorited items; don't cache entire catalog
- **Sync Strategy**: Automatic background sync when connectivity returns; user notified of conflicts

### Data Retention & Privacy (Principle VI)

- **Favorites Retention**: Locally stored indefinitely until user deletes; cloud favorites tied to account (deleted on account deletion)
- **Search History**: Kept for current session; cleared on app close or explicit delete
- **Telemetry**: None by default; opt-in only (not pre-checked) if analytics added in future

---

## Assumptions

1. **Existing Backend**: A functional OpenFoodFacts integration or proprietary product database exists; API contracts defined and tested before frontend implementation
2. **Authentication (Optional for MVP)**: App works in anonymous mode; account creation optional; if user logs in, favorites sync to cloud
3. **Image Hosting**: Product images are hosted on CDN with CORS enabled; image URLs are stable and won't break mid-session
4. **Certification Data Accuracy**: Certification data from OpenFoodFacts or certified sources; app displays "Community Verified" transparently when source is user-contributed
5. **Device Capabilities**: Device has at least 256MB RAM available; typical connected device (3G/4G/5G or WiFi)
6. **Platform APIs Available**: Kotlin Multiplatform can access native storage (SQLDelight), network stack (Ktor), and theme APIs for all target platforms
7. **No Third-Party Auth for MVP**: Authentication via username/password OR no authentication (anonymous mode)
8. **Product Count**: Catalog reasonably finite (up to 100K products); pagination strategy scales to this
9. **User Session Duration**: Average session 5-15 minutes; app doesn't need to store indefinitely long state
10. **Network Conditions**: Users may have intermittent connectivity (grocery store, rural areas); app must handle 2-3 second latencies gracefully
11. **ETL Schedule & Versioning**: Primary ETL full-refresh scheduled every 6 months (as currently practiced). Additionally, implement monthly incremental delta imports when supported by source (OpenFoodFacts) to reduce data drift. Product change history/snapshots retained for audit for 24 months; historical identifiers preserved for merges.

---

## Testing Strategy by Feature

### Catalog Feature Testing

**Unit Tests**:
- Pagination logic: nextPage(), hasnext(), deduplication algorithm
- Image placeholder rendering under simulated delays

**Integration Tests**:
- API contract: GET /products with various filter combinations
- Pagination token validity across page loads
- Scroll position restoration via navigation stack

**End-to-End Tests**:
- User scrolls catalog → pagination loads → images render → scroll position restored
- Pagination error → user taps retry → loads successfully
- App backgrounded/restored → catalog state preserved

**Performance Tests**:
- Catalog render time < 1.5s on mid-range device
- Scrolling FPS maintained > 50fps during pagination load

### Search & Filter Testing

**Unit Tests**:
- Debounce algorithm: request fired only once per 500ms
- UTF-8 normalization (café → cafe search still works)
- History list max 5 entries, FIFO eviction

**Integration Tests**:
- GET /products with search query parameter
- Filter chip combination (AND logic)
- Empty result set handling

**End-to-End Tests**:
- Type search text → verify debounce → results appear
- Tap category chip → instant filter
- Combine search + chip filters → results reflect both
- Close and reopen search → history populates

### Product Detail Testing

**Unit Tests**:
- Ingredient list formatting/parsing
- Certification badge rendering (all types)
- Data model validation

**Integration Tests**:
- GET /products/:id returns full product object
- Image loading error handling

**End-to-End Tests**:
- Tap product → detail opens with all content
- Ingredient list scrollable without image disappearing
- Add to Favorites → state changes → confirmation shows

### Offline Favorites Testing

**Unit Tests**:
- SQLDelight insertion/deletion logic
- Sync conflict resolution (last-write-wins)
- Offline detection (connectivity check)

**Integration Tests**:
- POST /users/:userId/favorites
- DELETE /users/:userId/favorites/:id
- POST /sync/favorites with conflict scenarios

**End-to-End Tests (Requires Manual Networking)**:
- Add favorites online → turn off network → Favorites tab still shows all
- Delete favorite offline → turn on network → sync detects deletion
- Uninstall app, reinstall → restore favorites from cloud (if logged in)

**Emulator Tests**:
- Network toggle via Android Emulator network simulation
- iOS Simulator network link conditioner

### Accessibility Testing

**Automated Scans**:
- Android Accessibility Scanner (lint-level violations)
- iOS Accessibility Inspector
- axe for Web version

**Manual Testing**:
- Screen reader (TalkBack/VoiceOver) navigation of all screens
- Keyboard-only navigation (no touch)
- Color blindness simulation (Deuteranopia filter)
- Text scaling to 1.5x → verify layout holds

**WCAG Compliance Verification**:
- Contrast ratios measured with Colour Contrast Analyzer
- Touch target sizes measured in design tool

### Responsive Design Testing

**Device Matrix**:
- Android: Phones (4.5"–6.7"), tablets (7"–10")
- iOS: iPhones (SE–Max), iPads (10"–13")
- Web: Chrome/Safari/Firefox at 320px, 768px, 1024px widths

**Orientation Testing**:
- Portrait → landscape → portrait (state preserved)
- Detail view in landscape → content not cut off

### Performance Testing

**Metrics to Test**:
- Catalog render time (target < 1.5s)
- Image load time per product (progressive)
- Memory usage over 30 min session (target < 150MB)
- Battery drain (1 hour = < 15% battery)
- Sync time for 500 favorites (target < 10s)

**Tools**:
- Android Profiler (JVM Memory, Network Profiler)
- Xcode Instruments (Memory, Disk I/O)
- Lighthouse (Web performance)

---

## Implementation Dependencies & Phasing

### Phase 1: Core Catalog & Search (Dependencies: Backend API)
- Catalog with pagination (FR-001 to FR-006)
- Search with debounce (FR-007 to FR-012)
- Basic product detail (FR-013 to FR-018)
- **Deliverable**: Testable, scrollable catalog with working search
- **Estimated Scope**: High (cross-platform UI, API integration, pagination logic)
- **Blockers**: Backend API contract finalized; product database populated

### Phase 2: Offline Favorites & Sync (Dependencies: Phase 1 + Local Storage DB)
- Local favorites persistence (FR-019 to FR-024)
- Sync mechanism (including conflict resolution)
- **Deliverable**: Users can browse and favorite products offline
- **Estimated Scope**: Medium (SQLDelight setup, sync algorithm, conflict handling)
- **Blockers**: Phase 1 complete; backend sync endpoint finalized

### Phase 3: Visual Accessibility & Theme (Dependencies: Phase 1 + Design System)
- Light/dark theming (FR-025 to FR-026)
- WCAG 2.1 AA compliance (FR-027 to FR-032)
- Screen reader integration
- **Deliverable**: Accessible app passing automated and manual WCAG audits
- **Estimated Scope**: Medium-High (design system definition, accessibility testing framework, platform-specific work)
- **Blockers**: Color palette defined; Compose Multiplatform accessibility APIs available

### Phase 4: Performance & Polish (Dependencies: Phases 1-3)
- Image optimization and lazy loading refinement
- Memory profiling and optimization
- Battery impact reduction
- Animation performance (motion-sensitive mode)
- **Deliverable**: App meets all performance targets; smooth on mid-range devices
- **Estimated Scope**: High (profiling, optimization, device testing matrix)

### Cross-Phase Requirements
- Backend API available (mocked or real) by end of Phase 1
- Kotlin Multiplatform build environment stable
- GitHub Actions CI/CD pipeline running (lint, build, test)
- Test data (product catalog) available for integration testing

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

**Functionality**
- **SC-001**: Users can browse catalog and see ≥ 50 products without manual pagination interactions
- **SC-002**: Search returns results in < 2 seconds for 90% of queries (p90); debounce verified via logs
- **SC-003**: Product detail view displays all required fields (image, ingredients, certifications) with zero missing data for ≥ 95% of products in test dataset
- **SC-004**: Users can add ≥ 20 products to favorites and access them offline without network connectivity
- **SC-005**: Sync process merges ≥ 100 local favorites to cloud account within 10 seconds on LTE network

**Performance**
- **SC-006**: App launches and displays initial catalog in ≤ 2 seconds on mid-range Android device (Snapdragon 778G or equivalent)
- **SC-007**: Catalog scroll maintains ≥ 50 FPS during pagination load (measured via ProfileCanvasFrameTime on Android, Core Animation on iOS)
- **SC-008**: Memory usage remains < 150MB over 1-hour continuous browsing session (catalog, search, detail cycle)
- **SC-009**: Battery drain from app usage is ≤ 15% per hour (measured on fresh 100% charge)

**Accessibility**
- **SC-010**: 100% of interactive elements (buttons, links, input fields) have touch targets ≥ 48x48dp (Android) or 44x44pt (iOS)
- **SC-011**: All text elements meet WCAG 2.1 AA contrast ratio (4.5:1 for normal, 3:1 for large text); 0 findings in Accessibility Scanner (Android) and Accessibility Inspector (iOS)
- **SC-012**: Screen reader navigation of all screens is complete and coherent; no missing labels or jumbled announcements
- **SC-013**: App functions correctly with device text scaling set to 1.5x (150%); no UI overflow or content hidden

**Offline & Sync**
- **SC-014**: App remains fully functional for ≥ 7 days without network access (using cached catalog and local favorites)
- **SC-015**: Sync conflict resolution succeeds for ≥ 100 simultaneous add/delete operations between client and server

**User Experience**
- **SC-016**: First-time user can add ≥ 5 products to favorites with no tutorial in < 3 minutes (usability test, 5 participant minimum)
- **SC-017**: ≥ 90% of test users successfully locate a specific product (given product name) via search within 2 attempts
- **SC-018**: Dark/Light mode respects device system setting and updates in-app without restart

**Data Integrity (Constitution Principle I)**
- **SC-019**: 100% of products with "Certified" badge have verified source (OpenFoodFacts, GFCO, CeliacUK); "Community Verified" products clearly labeled
- **SC-020**: Product certification data has associated verification timestamp; data > 1 year old marked as "May be outdated"

### Verification Method
- **Automated**: CI/CD pipeline (performance benchmarks, unit/integration test coverage)
- **Manual**: Accessibility audits (via Accessibility Scanner, Inspector, axe), usability testing (5–10 users per test)
- **Device Testing**: Mid-range Android device, iPhone SE, iPad, Web (desktop + mobile view)

---

## Alignment with Constitution Principles

This specification adheres to all 7 core principles:

1. **Data Integrity & Safety-First** (Principle I):
   - Certification data sourced and marked transparently (FR-016, SC-019)
   - No silent data degradation; unverified data labeled clearly
   - Required testing of data accuracy before release

2. **Multiplatform-First Architecture** (Principle II):
   - Shared business logic in commonMain; platform UI only in androidMain/iosMain/wasmJsMain
   - Unified data models and API contracts across all platforms
   - Contracts validated via integration tests (Phase 1)

3. **Performance-First Design** (Principle III):
   - Offline-first favorites (FR-019–FR-024) ensure function without perfect connectivity
   - Async image loading prevents UI blocking (FR-003)
   - API response time targets < 200ms (backend); frontend rendering < 1.5s (SC-006)
   - Pagination and debounce limit request overhead

4. **Test-Driven Development** (Principle IV):
   - Testing strategy defined by feature (Unit/Integration/E2E breakdown)
   - Minimum 80% coverage on core modules (pagination, search, sync logic)
   - Contract tests ensure API/UI compatibility before implementation

5. **CI/CD Automation** (Principle V):
   - CI pipeline validates lint, unit tests, integration tests, build artifacts
   - No merge without green builds (enforced)
   - Staging auto-deploys on main branch; production via explicit version tag

6. **User Privacy & Data Minimization** (Principle VI):
   - No telemetry or tracking by default (optional future feature, opt-in only)
   - Favorites stored locally first; synced to cloud only if user has account
   - Account creation optional for MVP
   - Session tokens expire in 30 days; refresh tokens rotated

7. **Accessibility & Inclusive Design** (Principle VII):
   - WCAG 2.1 AA compliance explicit in FR-025–FR-032 and SC-010–SC-013
   - Screen reader support, keyboard navigation, color-blind safe design
   - Touch target and contrast ratio minimums enforced

---

## Glossary & Technical Terms

- **Debounce (500ms)**: Wait 500ms after last keystroke before sending search request; prevents spam
- **Pagination Token**: Server-supplied cursor identifying exact page position; enables efficient pagination
- **Infinite Scroll**: Automatic pagination load triggered by scroll position (80% of current page)
- **Sync (Last-Write-Wins)**: Conflict resolution strategy: timestamp-based; most recent local/server action wins
- **WCAG 2.1 AA**: Web Content Accessibility Guidelines Level AA; includes contrast, keyboard, screen reader support
- **Touch Target**: Minimum area (48x48dp Android, 44x44pt iOS) for user to tap reliably
- **SQLDelight**: Kotlin type-safe database library; generates SQL queries at compile-time
- **Kotlin Multiplatform (KMP)**: Kotlin code shared across Android/iOS/Web/other platforms
- **Compose Multiplatform**: Jetpack Compose ported to iOS, Web, and desktop via Kotlin
- **OpenFoodFacts**: Open-source food product database; used for gluten-free product sourcing

---

**Specification Complete**

This specification is comprehensive, cross-platform compatible, and aligned with GluFreeListApp's constitutional principles. It provides clear user stories, testable functional requirements, measurable success criteria, and implementation guidance for all stakeholders.

Proceed to `/speckit.plan` to break down features into development tasks.

## Clarifications

### Session 2026-06-12

- Q1: Authentication and identity method → A) Email/password with email verification + JWT access/refresh tokens. MFA: Optional, not required for MVP. → A: Chosen to align with Constitution (JWT) and allow account optionality while keeping server-side token rotation and email verification for account recovery.

- Q2: Anonymous favorites that later login → B) Keep local and offer option to merge manually. → A: Chosen to avoid silent data loss; app will prompt users on first login to Merge / Keep local / Replace; merge is recommended default.

- Q3: Product identifier canonical → Use barcode/GTIN when present as external identifier for mapping; fallback to OpenFoodFacts id; maintain internal stable UUID as DB primary key. Handle code changes by keeping previousIdentifiers (aliases) and mapping redirects; merges require audit and preserve history.

- Q4: Favorites sync conflict resolution → A) Merge (union) of favorites per item, de-duplicated; deletions treated as tombstones with timestamp comparison. → A: Union minimizes data loss and suits favorites' additive nature; tombstones ensure intentional deletes are respected.

- Q5: ETL frequency & versioning → Full-refresh every 6 months (existing cadence) + monthly incremental deltas where available; retain product snapshots/history for 24 months for audit.

- Optional CI/CD (iOS/macOS): Yes — macOS builds run automatically on release tags; provide a manual workflow for PRs/QA to run macOS build on demand.

These clarifications have been applied to the spec in-line (Security, Offline Favorites, Product Entity, Sync contract, Assumptions). Each change is minimal and testable per the clarification rules.

