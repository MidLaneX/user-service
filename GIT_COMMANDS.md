# Git Commands Reference

This document provides the correct command order for initializing a Git repository with the default branch as main and connecting to a remote origin.

## 1. Initialize Git Repository with Main Branch

```bash
# Initialize a new Git repository
git init

# Set the default branch to main (if not already configured globally)
git config --global init.defaultBranch main

# Or rename the current branch to main if it was created as master
git branch -M main
```

## 2. Add and Commit Initial Files

```bash
# Add all files to staging area
git add .

# Create initial commit
git commit -m "Initial commit"
```

## 3. Connect to Remote Origin

```bash
# Add remote origin (replace with your repository URL)
git remote add origin https://github.com/username/repository-name.git

# Verify remote was added correctly
git remote -v
```

## 4. Push to Remote Repository

```bash
# Push to remote repository and set upstream
git push -u origin main

# For subsequent pushes, you can simply use:
git push
```

## 5. Pull Changes from Remote

```bash
# Pull latest changes from remote
git pull origin main

# Or if upstream is set:
git pull
```

## Complete Command Sequence

Here's the complete sequence in order:

```bash
# 1. Initialize repository
git init
git branch -M main

# 2. Add and commit files
git add .
git commit -m "Initial commit"

# 3. Connect to remote
git remote add origin https://github.com/username/repository-name.git

# 4. Push to remote
git push -u origin main

# 5. Pull changes (for future updates)
git pull origin main
```

## Additional Useful Commands

```bash
# Check repository status
git status

# View commit history
git log --oneline

# Check which branch you're on
git branch

# Create and switch to new branch
git checkout -b feature-branch-name

# Switch between branches
git checkout main
git checkout feature-branch-name

# Merge branch into main
git checkout main
git merge feature-branch-name

# Delete branch
git branch -d feature-branch-name
```

## Best Practices

1. Always pull before pushing when working with others
2. Use meaningful commit messages
3. Commit small, logical changes
4. Use branches for features and bug fixes
5. Review changes before committing with `git status` and `git diff`
