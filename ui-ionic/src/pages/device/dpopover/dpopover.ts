import { Component } from '@angular/core';
import { IonicPage, NavController, NavParams, ViewController } from 'ionic-angular';
import { OverviewPage } from '../../overview/overview';
import { DsettingsPage } from '../dsettings/dsettings';
import { AboutPage } from '../../about/about';

@Component({
  selector: 'page-dpopover',
  templateUrl: 'dpopover.html',
})
export class DpopoverPage {

  [x: string]: any;
  constructor(public viewCtrl: ViewController, public navCtrl: NavController) {
  }

  ionViewDidLoad() {
    console.log('ionViewDidLoad PopoverPage');
  }

  itemTapped2(event) {
    this.navCtrl.push(OverviewPage);
    this.viewCtrl.dismiss();
  }

  itemTapped(event) {
    this.navCtrl.push(DsettingsPage);
    this.viewCtrl.dismiss();
  }

  itemTapped3(event) {
    this.navCtrl.push(AboutPage);
    this.viewCtrl.dismiss();
  }



  close() {
    this.viewCtrl.dismiss();
  }
}
