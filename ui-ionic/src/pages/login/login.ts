import { Component, Injectable } from '@angular/core';
import { NgModule } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { IonicPage, NavController, NavParams, ToastController } from 'ionic-angular';
import { OverviewPage } from '../overview/overview';
import { Websocket } from '../websocket/websocket';
import { NgIf } from '@angular/common';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { takeUntil } from 'rxjs/operator/takeUntil';
import { Service } from '../shared/service/service';



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

  constructor(public toastCtrl: ToastController,
    public navCtrl: NavController,
    public navParams: NavParams,
    public websocket: Websocket,
    private service: Service) { }



  public password: string;
  public loginSubject;
  private unsub = new Subject<void>()
  private savecookie: boolean;


  ionViewDidLoad() {
    console.log('ionViewDidLoad LoginPage');

  }

  ionViewWillEnter() {
    console.log("HIER WIRD GEENTERT")

    this.websocket.isLoggedIn.takeUntil(this.unsub).subscribe(value => {
      console.log("LOGIN: " + value);
      if (value == "loginSuccessful") { this.navCtrl.push(OverviewPage) }

      else if (value == "loginDenied") { this.noaccess() }


    })


  }


  ionViewWillLeave() {
    this.unsub.next();
    this.unsub.complete();
    console.log("LEAVEEEE");
  }
  ionViewDidLeave() {
  }


  savelogin() {

    console.log("SCHOKOLOKOKOKOKOKO", this.savecookie);
    if (this.savecookie == true) {

    }

  }


  doLogin() {
    let password: string = this.password;
    this.websocket.logIn(password);
  }



  noaccess() {
    let toast = this.toastCtrl.create({
      message: 'Falsches Passwort',
      position: 'top',
      duration: 2000,
      cssClass: 'toast'
    });
    toast.present();
  }
}




