# Requirements: Infinite Scroll Pagination

## Business Requirements
- As a user scrolling through the food catalog, I want the next set of items to load automatically without pressing a button.
- I want a visual loading indicator at the bottom to know more items are being fetched.
- Rapid scrolling must be safe from double-fetching issues.
- Changing search terms or category chips must reset pagination back to page 1.

## Acceptance Criteria
- [x] Users can scroll to the bottom of the visible items and the app automatically fetches the next 50 items.
- [x] A loading indicator appears at the bottom while fetching.
- [x] Prevents redundant network calls (no duplicate fetches of the same page).
- [x] Filtering/searching resets the list and pagination state back to page 1.
- [x] The feature operates cleanly without crashing or experiencing index-out-of-bounds exceptions.