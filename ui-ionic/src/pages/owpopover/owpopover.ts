import { Component } from '@angular/core';
import { IonicPage, NavController, NavParams, ViewController } from 'ionic-angular';
import { GsettingsPage } from '../gsettings/gsettings';
import { DoverlayPage } from '../device/doverlay/doverlay';
import { AboutPage } from '../about/about';


@Component({
  selector: 'page-owpopover',
  templateUrl: 'owpopover.html',
})
export class OwpopoverPage {

  [x: string]: any;
  constructor(public navCtrl: NavController, public navParams: NavParams, public viewCtrl: ViewController,) {
  }

  ionViewDidLoad() {
    console.log('ionViewDidLoad OwpopoverPage');
  }


  itemTapped(event) {
    this.navCtrl.push(GsettingsPage);
    this.viewCtrl.dismiss();
  }

  itemTapped2(event) {
    this.navCtrl.push(AboutPage);
    this.viewCtrl.dismiss();
  }
}
