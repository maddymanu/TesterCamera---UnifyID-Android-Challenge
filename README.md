# TesterCamera---UnifyID-Android-Challenge
How to Run:
  1. Import project into Android Studio.
 2. Connect Android Device.
 3. Click ‘Run’.

## Note:
 If the camera doesn’t start, you might need to go to settings->apps->testerCamera and manually add permissions. This shouldn’t happen though, and is highly unlikely.

### Further Considerations:
  Issue 1: Showing/Not showing the image view after every click. 
 The Surface view got stuck after the first click and wasn’t getting updated after each subsequent click.

  Issue 2: Data Storage:
 For now, I busted used a 3rd party library to store the images. Its solar to SharedPreferneces.  Link:https://github.com/pilgr/Paper

  Issue 3: Camera Permissions
 It took a while to figure out which versions require which methodology, coz just adding the permissions to the android manifest wasn’t working.

  Issue 4: How to encode images. A quick google and stack overflow search showed the most straightforward way. Given adequate time, a cryptographic algorithm could be written here to secure the images further. 

   For now, I didn’t put a way to retrieve the images, but I did make a dummy function that printed out the encrypted strings. 
