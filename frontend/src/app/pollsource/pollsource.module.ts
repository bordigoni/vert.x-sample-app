import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PollsourceListComponent} from './pollsource-list/pollsource-list.component';
import {PollsourceEditComponent} from './pollsource-edit/pollsource-edit.component';
import {FormsModule} from '@angular/forms';
import {PollSourceRoutingModule} from './pollsource-routing.module';
import {ClientModule} from '../client/client.module';
import {PollsourceService} from './pollsource.service';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    PollSourceRoutingModule,
    ClientModule
  ],
  providers: [PollsourceService],
  declarations: [PollsourceListComponent, PollsourceEditComponent]
})
export class PollsourceModule {
}
