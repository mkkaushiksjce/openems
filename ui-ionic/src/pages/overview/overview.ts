import { Component } from '@angular/core';
import { NavController } from 'ionic-angular'
import { PopoverController } from 'ionic-angular';
import { DoverlayPage } from '../device/doverlay/doverlay';
import { DpopoverPage } from '../device/dpopover/dpopover';
import { OwpopoverPage } from '../owpopover/owpopover';


@Component({
  selector: 'page-overview',
  templateUrl: 'overview.html'
})
export class OverviewPage {

  showSearchBar: boolean = false;

  [x: string]: any;
  constructor(public navCtrl: NavController, public popoverCtrl: PopoverController) {

  }

  itemTapped(event) {
    this.navCtrl.push(DoverlayPage);
  }
  presentPopover(myEvent) {
    let popover = this.popoverCtrl.create(OwpopoverPage);
    popover.present({
      ev: myEvent
    });
  }
}
