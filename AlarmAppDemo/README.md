# Dynamic Alarm App Demo
## Purpose
This web application is intended to demonstrate an integration of the [Common Core Ontology](https://github.com/CommonCoreOntology/CommonCoreOntologies) with [Solid](https://solid.mit.edu/).

## Use & Requirements

### Requirements
Use of this app requires the user logging in to have a CCO conformant profile stored at https://\<username>.solid.community/profile/card. The app will NOT work otherwise. 

### Usage
After downloading the repo, head to the <b>/AlarmAppDemo</b> directory and run the following commands:<br>
<b>
npm install<br>
npm start<br>
</b>

Once the server is running, the react app will open in a browser. There will be a login page displayed. Choose <b>Login with provider</b>, this will launch a Solid auth window in which you may authenticate.

The next page is a simple explanation of what the app does, click <b>Setup Alarm</b>. 

## Available Scripts

In the project directory, you can run:

### `npm start`

Runs the app in the development mode.<br>
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

## Credits
This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app). More specifically, it was created using a [Solid app template.](https://solid.inrupt.com/docs/writing-solid-apps-with-react)
