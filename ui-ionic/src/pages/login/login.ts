import { Component } from '@angular/core';
import { IonicPage, NavController, NavParams } from 'ionic-angular';
import { OverviewPage } from '../overview/overview';
import { Websocket } from '../websocket/websocket';


/**
 * Generated class for the LoginPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@Component({
  selector: 'page-login',
  templateUrl: 'login.html',
})
export class LoginPage {



  constructor(public navCtrl: NavController, public navParams: NavParams, public websocket: Websocket) {
  }

  ionViewDidLoad() {
    console.log('ionViewDidLoad LoginPage');

  }
  login() {
    this.websocket.login();
    this.navCtrl.push(OverviewPage);
  }





  ueberschreiben() {
    console.log(this.websocket.test)
    this.websocket.test = "Hallo Stefan";
    console.log(this.websocket.test)
  }



}
