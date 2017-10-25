import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ClientListComponent} from './client-list/client-list.component';
import {ClientEditComponent} from './client-edit/client-edit.component';
import {FormsModule} from '@angular/forms';
import {ClientService} from './client.service';
import {ClientRoutingModule} from './client-routing.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ClientRoutingModule
  ],
  providers: [ClientService],
  declarations: [ClientListComponent, ClientEditComponent]
})
export class ClientModule {
}
