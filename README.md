# Monthly Commitment & Cash Flow Tracker (Android APK)

A clean, offline-first Android app built with Kotlin & Jetpack Compose to track **Salary**, **Business Profit**, and **Monthly Commitments** (loans, instalments, bills, subscriptions) so you always know your exact **Balance Left**.

## Key Features
- **Salary & Business Profit Tracking**: Add multiple income sources to calculate total monthly inflow.
- **Rename Any Payment**: Tap any entry or its edit icon to rename it, update the amount, or change its due date.
- **Category Sorting & Filtering**: Filter by *Salary*, *Business Profit*, *Loans & Instalments*, *Utilities & Bills*, *Subscriptions*, *Insurance / Takaful*, or type your own custom category. Sort by Category, Highest RM, or Due Day.
- **Paid / Unpaid Checklist**: Tick off commitments as you pay them each month, with a one-tap reset button at the top for the next month.
- **Automated GitHub Release APK**: Pushing this repository to GitHub automatically compiles, signs, and publishes a downloadable `.apk` file.

## How to Build & Download the Release APK on GitHub
1. Create a new repository on [GitHub](https://github.com/new).
2. Extract this ZIP file and upload all files (including the hidden `.github` folder) to your repository.
3. Go to the **Actions** tab in your GitHub repository to watch **Build Android Release APK** run (takes ~2–3 minutes).
4. Once complete, download `app-release.apk` directly from the **Releases** section on the right sidebar of your repository (or under **Artifacts** in the workflow run) and install it on your Android phone.
