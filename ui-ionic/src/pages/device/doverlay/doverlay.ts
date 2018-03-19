import { Component } from '@angular/core';
import { IonicPage, NavController, NavParams } from 'ionic-angular';
import { PopoverController } from 'ionic-angular';
import { DpopoverPage } from '../dpopover/dpopover';
import { OwpopoverPage } from '../../owpopover/owpopover';
import { DsettingsPage } from '../dsettings/dsettings';
import { MonitorPage } from '../monitor/monitor';
import { TablePage } from '../table/table';
import { HistoryPage } from '../history/history';


@Component({
  template: `
  <ion-header>
    <ion-navbar>
      <ion-title>Tabsx</ion-title>
    </ion-navbar>
  </ion-header>
  <ion-content></ion-content>
  `
})
export class TabContentPage {
  constructor(public popoverCtrl: PopoverController) { }
  
  presentPopover(myEvent) {
    let popover = this.popoverCtrl.create(DpopoverPage);
    popover.present({
      ev: myEvent
    });
  }
}



@IonicPage()
@Component({
  selector: 'page-doverlay',
  templateUrl: 'doverlay.html',

})
export class DoverlayPage {

  [x: string]: any;
  tab1 = MonitorPage;
  tab2 = TablePage;
  tab3 = HistoryPage;

  constructor(public navCtrl: NavController,public popoverCtrl: PopoverController, public navParams: NavParams) {
  }

  ionViewDidLoad() {
    console.log('ionViewDidLoad DoverlayPage');
  }

  itemTapped(event) {
    this.navCtrl.push(DsettingsPage);
  }
  presentPopover(myEvent) {
    let popover = this.popoverCtrl.create(DpopoverPage);
    popover.present({
      ev: myEvent
    });
  }

}


