#Module 03 Assignment 03: Programming Assignment 1

Before we get started,we need to make sure that we have the following prerequisites in place:

AWS Account: We need an AWS account. Follow the steps mentioned in Canvas

Step 1: Clone the Repository
Start by cloning the repository containing the project. You can use the git clone command to do this. Open your terminal or command prompt and run:

git clone https://github.com/your-username/aws-object-text-recognition.git


This will create a local copy of the project on your machine.

Step 2: Configure AWS Credentials

1. Log in to your AWS account:
Go to https://aws.amazon.com/ and sign in with your AWS account credentials.

2. Access IAM:
In the AWS Management Console, find "IAM" by searching in the bar at the top or under "Security, Identity, & Compliance."

3. Create a Group:
Click on "Groups" in the left panel.
Click "Create New Group."
Name the group, e.g., "AppGroup."
Click "Next Step."

4. Add Permissions:
"AmazonRekognitionFullAccess" (for Rekognition)
"AmazonS3FullAccess" (for S3)
"AmazonSQSFullAccess" (for SQS)
Click "Next Review."

5. Review & Create Group:
Review the settings.
Click "Create Group."

6. Create a User:
Click "Users" in the left panel.
Click "Add User."
Name the user, e.g., "mohan"
Choose "Programmatic access" for access keys.
Click "Next: Permissions."

7. Add User to the Group:
In the "Add user to group" step, add the user to "AppGroup."
Click "Next: Review."

8. Review & Create User:
Review the user's settings.
Click "Create user."

9. Generate Access Keys:
After creating the user, we will see a confirmation screen.
Click "Download .csv" to save the access key and secret access key for "AppUser." Store these securely.
Now, we have configured AWS IAM for your application, creating a group with permissions and an IAM user with access keys for authentication. These keys will be used in your application's AWSConfig to interact with AWS services securely.


Step 3: AWS S3 Setup
To use AWS S3 for storing the images, make sure to have an S3 bucket set up. We can create a new bucket in the AWS S3 console.

Navigate to the S3 service and create a new S3 bucket and name it "object-text-reko" (you can choose a different name, just remember to update it in the code).

Upload images to this bucket that you want to process for object and text recognition.

Step 4: Object Recognition
The project performs object recognition to detect objects like cars in your images. It will send a message to the SQS queue when a car is detected.

Open the project in the IDE.

Run the RekognitionObjectApplication class. This will start the object recognition process.

The application will list the objects in the "object-text-reko" S3 bucket, detect objects, and send messages to the SQS queue for cars detected with high confidence.

Step 5: Text Detection
The text detection part listens to the SQS queue for messages containing keywords or identifiers. It scans images for text when it receives a message.

Run the RekognitionTextApplication class in your IDE.

This part will listen to the SQS queue "queue-cars.fifo" created in the object recognition step.

When it receives a message, it will scan the images in the "object-text-reko" S3 bucket for the specified keyword and detect any text in those images.
